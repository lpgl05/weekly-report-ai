package com.gateway.client.service.impl;

import com.alibaba.fastjson2.JSON;
import com.gateway.client.domain.FeiShuLocalExcelResult;
import com.gateway.client.domain.FeiShuLocalMergeCell;
import com.gateway.client.domain.FeiShuSheet;
import com.gateway.client.service.IDataProcessingService;
import com.gateway.common.utils.AjaxResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 数据处理服务实现类
 */
@Service
public class DataProcessingServiceImpl implements IDataProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DataProcessingServiceImpl.class);

    @Autowired
    private DataProcessingFeiShuServiceImpl feiShuService;

    @Autowired
    private DataProcessingExcelServiceImpl excelService;

    /**
     * 静态HashSet对象，用于存储允许处理的文件名
     */
    private static final Map<String, String> ALLOWED_FILE_NAMES = new HashMap<>() {{
        put("【老】30专线组服秘数据追踪.xlsx", "Fh6OsbtluhNM0CtyuKccC9XOn9c");
        put("【新】30专线组服秘数据追踪.xlsx", "Kz8OsOZIOhEdRettK9TcMRtQnXd");
        put("单场直播转发及观看看板（优先找客雷达）.xlsx", "EJs3sjdEDhalxJtoMeYcEYTEnzc");
        put("观看直播统计.xlsx", "DggJsn4LbhHibytSgHsc1Im0n4e");
        put("冷线索.xlsx", "ZCF0spcQehqKDItr1r2cr6pxnud");
        put("冷线索添加标签和填写跟进总数.xlsx", "XtkxsmOj7hi21UtA8mGc9857n8f");
        put("大区热线索未读.xlsx", "R9KJsCb4Mh0CvUtcsEscHbtdnyh");
        put("商机一码通.xlsx", "OcCSsuNZ4hYGGYtdGhgcBTZ1nxc");
        put("团队看板（员工数）.xlsx", "ZkjEscPeshQgArt5Pq7ceb8anPf");
        put("销售AI助手-托管数据.xlsx", "EXEAs18NGhdN1ytf6lpc6kETnVf");
        put("营收超1亿明细.xlsx", "Ghe0s5ydjh6kGstzZHecL5H4nzh");
        put("员工明细看板.xlsx", "Qcd0s81NohlT9HtkX6bc3q7onUd");
        put("找客雷达（考核月数据）.xlsx", "TuG5sh8zOhKEeJtuunhcEXTKnTb");
        put("中间库id填写情况.xlsx", "Cxz6s0WpIh9wr1t9MnFcp4LFnBc");
        put("中间库客户.xlsx", "F31hsRl42hhUXltIwDecDDE8nLz");
        put("本周新增热线索首次参会及成交.xlsx", "GZNZsG7MlhhX8dt4ZHYcv5KanuH");
        put("累计热线索首次参会及成交.xlsx", "CDoWshU47hYJcTtxcfmcLnp0nAg");
        put("组织架构.xlsx", "QOfCs6sRFhA7yHtZHIucjJmqn4f");
    }};

    /**
     * 规范化文件名：
     * - 去除首尾空格
     * - 统一扩展名小写
     * - 去除扩展名前的多余空格（如 "员工明细看板 .xlsx" → "员工明细看板.xlsx"）
     */
    private String normalizeFileName(String name) {
        if (name == null) return null;
        String n = name.trim();
        // 统一扩展名小写
        n = n.replaceAll("\\.XLSX$", ".xlsx").replaceAll("\\.XLS$", ".xls");
        // 去除扩展名前的多余空格
        n = n.replaceAll("\\s+\\.xlsx$", ".xlsx").replaceAll("\\s+\\.xls$", ".xls");
        return n;
    }

    /**
     * 根据规范化值查找原始配置key（用于保持与原始ALLOWED_FILE_NAMES键一致）
     */
    private String findOriginalKeyByNormalized(String normalized) {
        if (normalized == null) return null;
        for (String key : ALLOWED_FILE_NAMES.keySet()) {
            if (normalizeFileName(key).equals(normalized)) {
                return key;
            }
        }
        return null;
    }

    /**
     * 调用飞书接口
     */
    @Override
    public AjaxResult invokeFeiShu(MultipartFile[] files) {
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(files.length, 10));
        try {
            log.info("processing-调用飞书接口-service开始");

            // 文件名称校验
            Set<String> repeatMap = new HashSet<>();
            AjaxResult checkResult = checkFile(files, repeatMap);
            if (checkResult.isError()) {
                log.info("processing-调用飞书接口-结束,result={}", JSON.toJSONString(checkResult));
                return checkResult;
            }

            // 获取飞书Token
            String feiShuAccessToken = feiShuService.getFeiShuAccessToken();
            if (StringUtils.isBlank(feiShuAccessToken)) {
                log.info("processing-调用飞书接口-结束-获取飞书Token失败");
                return AjaxResult.error("获取飞书Token失败");
            }
            List<Future<AjaxResult>> futures = new ArrayList<>();

            // 遍历处理文件
            for (MultipartFile file : files) {
                Future<AjaxResult> future = executorService.submit(() -> dualFile(feiShuAccessToken, file));
                futures.add(future);
            }

            // 收集所有线程结果
            List<String> errorMessages = new ArrayList<>();
            for (Future<AjaxResult> future : futures) {
                try {
                    AjaxResult result = future.get();
                    if (result.isError()) {
                        errorMessages.add(JSON.toJSONString(result));
                    }
                } catch (Exception e) {
                    log.error("处理文件时发生异常", e);
                    errorMessages.add("文件处理异常: " + e.getMessage());
                }
            }

            // 检查是否有错误
            if (!errorMessages.isEmpty()) {
                log.info("processing-调用飞书接口-结束-处理文件失败,errors={}", errorMessages);
                return AjaxResult.error("部分文件处理失败: " + String.join("; ", errorMessages));
            }

            for (String fileName : ALLOWED_FILE_NAMES.keySet()) {
                if(repeatMap.contains(fileName)){
                    continue;
                }
                dualOtherFile(feiShuAccessToken, fileName);
            }

            AjaxResult result = AjaxResult.success("飞书接口调用成功");
            log.info("processing-调用飞书接口-结束-成功,result={}", JSON.toJSONString(result));
            return result;
        } catch (Exception e) {
            log.error("processing-调用飞书接口-异常", e);
            AjaxResult errorResult = AjaxResult.error("飞书接口调用失败");
            log.info("processing-调用飞书接口-结束-异常,result={}", JSON.toJSONString(errorResult));
            return errorResult;
        } finally {
            // 确保线程池被正确关闭
            executorService.shutdown();
        }
    }

    @Override
    public List<String> getAllowedFileNames() {
        return new ArrayList<>(ALLOWED_FILE_NAMES.keySet());
    }

    /**
     * 文件名称校验
     */
    private AjaxResult checkFile(MultipartFile[] files, Set<String> repeatMap) {
        if (files == null || files.length == 0) {
            return AjaxResult.error("请上传文件");
        }

        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (filename == null || filename.isEmpty()) {
                return AjaxResult.error("文件名不能为空");
            }
            // 规范化与容错：允许扩展名前多余空格、扩展名大小写不同等情况
            String normalized = normalizeFileName(filename);
            String originalKey = ALLOWED_FILE_NAMES.containsKey(filename) ? filename : findOriginalKeyByNormalized(normalized);
            if (originalKey == null) {
                return AjaxResult.error("请上传正确的文件，错误文件名：" + filename);
            }
            if (repeatMap.contains(originalKey)) {
                return AjaxResult.error("请勿重复上传文件，错误文件名：" + filename);
            }
            // 使用原始配置key记录重复集合，保证后续 dualOtherFile 跳过逻辑正确
            repeatMap.add(originalKey);
        }
        return AjaxResult.success();
    }

    /**
     * 文件处理
     */
    private AjaxResult dualFile(String accessToken, MultipartFile file) {
        String filename = file.getOriginalFilename();
        String spreadsheetToken = ALLOWED_FILE_NAMES.get(filename);
        log.info("processing-调用飞书接口-处理文件-开始,filename={}", filename);
        try {
            // 获取飞书表格Sheet
            Map<String, FeiShuSheet> feishuSheetMap = feiShuService.getFeishuSheetMap(spreadsheetToken);
            if (feishuSheetMap.isEmpty()) {
                log.info("processing-调用飞书接口-处理文件-获取飞书表格Sheet-失败,filename={}", filename);
                return AjaxResult.error("获取飞书表格Sheet失败，文件名：" + filename);
            }

            // 读取本地Excel中所有Sheet的名称
            List<String> localSheetNames = excelService.getLocalExcelSheetNames(file);
            if (localSheetNames.isEmpty()) {
                log.info("processing-调用飞书接口-处理文件-读取本地Excel中所有Sheet的名称-失败,filename={}", filename);
                return AjaxResult.error("获取" + filename + "Sheet失败");
            }

            // 遍历本地Sheet，同步到飞书
            for (String localSheetName : localSheetNames) {
                if (!feishuSheetMap.containsKey(localSheetName)) {
                    log.info("processing-调用飞书接口-处理文件-匹配飞书Sheet-跳过,filename={},localSheetName={}",
                            filename, localSheetName);
                    continue;
                }
                FeiShuSheet feiShuSheet = feishuSheetMap.get(localSheetName);
                String sheetId = feiShuSheet.getSheetId();
                // 读取本地指定sheet
                FeiShuLocalExcelResult result = excelService.readBySheetName(file, localSheetName);
                // 初始化行列
                initFeiShuSheet(spreadsheetToken, sheetId, accessToken, feiShuSheet, result);
                // 插入数据
                updateFeiShuSheet(spreadsheetToken, sheetId, accessToken, result);
                // 合并数据
                mergeFeiShuSheet(spreadsheetToken, sheetId, accessToken, result);
            }
            // 遍历飞书表格Sheet映射
            for (Map.Entry<String, FeiShuSheet> entry : feishuSheetMap.entrySet()) {
                if (localSheetNames.contains(entry.getKey())) {
                    continue;
                }
                FeiShuSheet feiShuSheet = entry.getValue();
                String sheetId = feiShuSheet.getSheetId();
                // 初始化行列
                initOtherFeiShuSheet(spreadsheetToken, sheetId, accessToken, feiShuSheet);
            }

        } catch (Exception e) {
            log.error("processing-调用飞书接口-处理文件-异常,filename={}", filename, e);
            return AjaxResult.error("处理" + filename + "文件失败");
        }
        log.info("processing-调用飞书接口-处理文件-结束,filename={}", filename);
        return AjaxResult.success();
    }


    /**
     * 处理其他文件
     */
    private AjaxResult dualOtherFile(String accessToken, String filename) {
        String spreadsheetToken = ALLOWED_FILE_NAMES.get(filename);
        log.info("processing-调用飞书接口-处理其他文件-开始,filename={}", filename);
        try {
            // 获取飞书表格Sheet
            Map<String, FeiShuSheet> feishuSheetMap = feiShuService.getFeishuSheetMap(spreadsheetToken);
            if (feishuSheetMap.isEmpty()) {
                log.info("processing-调用飞书接口-处理其他文件-获取飞书表格Sheet-失败,filename={}", filename);
                return AjaxResult.error("获取飞书表格Sheet失败，文件名：" + filename);
            }
            // 遍历飞书表格Sheet映射
            for (Map.Entry<String, FeiShuSheet> entry : feishuSheetMap.entrySet()) {
                FeiShuSheet feiShuSheet = entry.getValue();
                String sheetId = feiShuSheet.getSheetId();
                // 初始化行列
                initOtherFeiShuSheet(spreadsheetToken, sheetId, accessToken, feiShuSheet);
            }

        } catch (Exception e) {
            log.error("processing-调用飞书接口-处理其他文件-异常,filename={}", filename, e);
            return AjaxResult.error("处理" + filename + "文件失败");
        }
        log.info("processing-调用飞书接口-处理其他文件-结束,filename={}", filename);
        return AjaxResult.success();
    }

    /**
     * 初始化行列
     */
    private void initFeiShuSheet(String spreadsheetToken, String sheetId, String accessToken, FeiShuSheet feiShuSheet,
            FeiShuLocalExcelResult result) {
        feiShuService.addRow(spreadsheetToken, sheetId, accessToken, 1, "ROWS");
        Integer rowCount = feiShuSheet.getRowCount();
        Integer columnCount = feiShuSheet.getColumnCount();
        while (rowCount > 0) {
            if (rowCount > 5000) {
                feiShuService.deleteRow(spreadsheetToken, sheetId, accessToken, 1, 5000, "ROWS");
                rowCount = rowCount - 5000;
            } else {
                feiShuService.deleteRow(spreadsheetToken, sheetId, accessToken, 1, rowCount, "ROWS");
                rowCount = 0;
            }
        }
        while (columnCount > 1) {
            if (columnCount > 5000) {
                feiShuService.deleteRow(spreadsheetToken, sheetId, accessToken, 1, 5000, "COLUMNS");
                columnCount = columnCount - 5000;
            } else {
                feiShuService.deleteRow(spreadsheetToken, sheetId, accessToken, 1, columnCount - 1, "COLUMNS");
                columnCount = 1;
            }
        }
        int maxRowIndex = result.getMaxRowIndex();
        int maxColumnIndex = result.getMaxColumnIndex();
        while (maxRowIndex > 0) {
            if (maxRowIndex > 5000) {
                feiShuService.addRow(spreadsheetToken, sheetId, accessToken, 5000, "ROWS");
                maxRowIndex = maxRowIndex - 5000;
            } else {
                feiShuService.addRow(spreadsheetToken, sheetId, accessToken, maxRowIndex, "ROWS");
                maxRowIndex = 0;
            }
        }
        while (maxColumnIndex > 0) {
            if (maxColumnIndex > 5000) {
                feiShuService.addRow(spreadsheetToken, sheetId, accessToken, 5000, "COLUMNS");
                maxColumnIndex = maxColumnIndex - 5000;
            } else {
                feiShuService.addRow(spreadsheetToken, sheetId, accessToken, maxColumnIndex, "COLUMNS");
                maxColumnIndex = 0;
            }
        }
    }

    /**
     * 初始其他化行列
     */
    private void initOtherFeiShuSheet(String spreadsheetToken, String sheetId, String accessToken,
            FeiShuSheet feiShuSheet) {
        feiShuService.addRow(spreadsheetToken, sheetId, accessToken, 1, "ROWS");
        Integer rowCount = feiShuSheet.getRowCount();
        while (rowCount > 0) {
            if (rowCount > 5000) {
                feiShuService.deleteRow(spreadsheetToken, sheetId, accessToken, 1, 5000, "ROWS");
                rowCount = rowCount - 5000;
            } else {
                feiShuService.deleteRow(spreadsheetToken, sheetId, accessToken, 1, rowCount, "ROWS");
                rowCount = 0;
            }
        }
    }

    /**
     * 更新行列数据
     */
    private void updateFeiShuSheet(String spreadsheetToken, String sheetId, String accessToken,
            FeiShuLocalExcelResult result) {
        int maxColumnIndex = result.getMaxColumnIndex() + 1;
        int maxRowIndex = result.getMaxRowIndex() + 1;
        String excelColumn = convertToExcelColumn(maxColumnIndex);
        // 单次处理数
        if (maxRowIndex == 0 || maxColumnIndex == 0) {
            return;
        }
        List<List<String>> dataList = result.getDataList();
        int size = dataList.size();
        int index = 0;
        while (size > 0) {
            Map<String, Object> valueRange = new HashMap<>();
            if (size > 5000) {
                valueRange.put("range", sheetId + "!A" + (index + 1) + ":" + excelColumn + (index + 5000));
                List<List<Object>> values = new ArrayList<>();
                for (int i = index; i < (index + 5000); i++) {
                    List<Object> rowData = new ArrayList<>(dataList.get(i));
                    values.add(rowData);
                }
                valueRange.put("values", values);
                feiShuService.updateRow(spreadsheetToken, accessToken, valueRange);
                size -= 5000;
                index += 5000;
            } else {
                valueRange.put("range", sheetId + "!A" + (index + 1) + ":" + excelColumn + (index + size));
                List<List<Object>> values = new ArrayList<>();
                for (int i = index; i < (index + size); i++) {
                    List<Object> rowData = new ArrayList<>(dataList.get(i));
                    values.add(rowData);
                }
                valueRange.put("values", values);
                feiShuService.updateRow(spreadsheetToken, accessToken, valueRange);
                size = 0;
            }
        }
    }

    /**
     * 合并数据
     */
    private void mergeFeiShuSheet(String spreadsheetToken, String sheetId, String accessToken,
            FeiShuLocalExcelResult result) {
        List<FeiShuLocalMergeCell> mergeRegions = result.getMergeRegions();
        if (CollectionUtils.isEmpty(mergeRegions)) {
            return;
        }
        for (FeiShuLocalMergeCell mergeCell : mergeRegions) {
            String valueRange = sheetId + "!" + convertMergeCellToRange(mergeCell);
            feiShuService.mergeRow(spreadsheetToken, accessToken, valueRange);
        }
    }

    /**
     * 将合并单元格信息转换为Excel坐标格式
     */
    private String convertMergeCellToRange(FeiShuLocalMergeCell mergeCell) {
        // 转换起始列索引为Excel列标
        String firstColumn = convertToExcelColumn(mergeCell.getFirstColumn() + 1);
        // 转换结束列索引为Excel列标
        String lastColumn = convertToExcelColumn(mergeCell.getLastColumn() + 1);

        // 构造Excel坐标格式：起始列+起始行:结束列+结束行
        return firstColumn + (mergeCell.getFirstRow() + 1) + ":" +
                lastColumn + (mergeCell.getLastRow() + 1);
    }

    /**
     * 将数字列索引转换为Excel列标（1对应A，2对应B，...，26对应Z，27对应AA，以此类推）
     */
    private String convertToExcelColumn(int columnIndex) {
        StringBuilder columnName = new StringBuilder();
        while (columnIndex > 0) {
            int remainder = (columnIndex - 1) % 26;
            columnName.insert(0, (char) ('A' + remainder));
            columnIndex = (columnIndex - 1) / 26;
        }
        return columnName.toString();
    }

}
