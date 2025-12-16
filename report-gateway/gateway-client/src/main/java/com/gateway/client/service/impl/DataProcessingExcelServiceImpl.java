package com.gateway.client.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.util.ListUtils;
import com.gateway.client.domain.FeiShuLocalExcelResult;
import com.gateway.client.domain.FeiShuLocalMergeCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据处理服务实现类-读取本地excel 工具类
 */
@Service
public class DataProcessingExcelServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(DataProcessingExcelServiceImpl.class);

    /**
     * 从上传的Excel文件中获取所有Sheet名称
     */
    public List<String> getLocalExcelSheetNames(MultipartFile multipartFile) throws IOException {
        // 存储所有 Sheet 名称的列表
        List<String> sheetNames = new ArrayList<>();
        ExcelReaderBuilder excelReaderBuilder = EasyExcel.read(multipartFile.getInputStream());
        ExcelReader excelReader = excelReaderBuilder.build();
        List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
        for (ReadSheet sheet : sheets) {
            sheetNames.add(sheet.getSheetName());
        }
        return sheetNames;
    }

    /**
     * 按sheet名称读取Excel
     */
    public FeiShuLocalExcelResult readBySheetName(MultipartFile file, String sheetName) throws IOException {
        FeiShuLocalExcelResult result = new FeiShuLocalExcelResult();
        result.setTargetSheetName(sheetName);

        try (InputStream inputStream = file.getInputStream()) {
            ReadListener<Map<Integer, ReadCellData<?>>> listener = new ReadListener<>() {
                private final List<List<String>> dataList = ListUtils.newArrayList();

                @Override
                public void invoke(Map<Integer, ReadCellData<?>> rowData, AnalysisContext context) {

                    // 转换当前行为字符串列表
                    List<String> rowList = ListUtils.newArrayList();
                    int currentMaxCol = rowData.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);

                    for (int col = 0; col <= currentMaxCol; col++) {
                        Object cellData = rowData.get(col);
                        String cellValue = "";
                        if (cellData instanceof ReadCellData) {
                            cellValue = ((ReadCellData<?>) cellData).getStringValue();
                        } else if (cellData != null) {
                            cellValue = cellData.toString();
                        }
                        rowList.add(cellValue);
                    }
                    dataList.add(rowList);

                    // 更新最大行索引
                    int currentRow = dataList.size() - 1;
                    result.setMaxRowIndex(Math.max(result.getMaxRowIndex(), currentRow));

                    // 更新最大列索引
                    result.setMaxColumnIndex(Math.max(result.getMaxColumnIndex(), currentMaxCol));

                }

                @Override
                public void extra(CellExtra extra, AnalysisContext context) {
                    if (extra.getType() == CellExtraTypeEnum.MERGE) {
                        FeiShuLocalMergeCell mergeCell = new FeiShuLocalMergeCell();
                        mergeCell.setFirstRow(extra.getFirstRowIndex());
                        mergeCell.setLastRow(extra.getLastRowIndex());
                        mergeCell.setFirstColumn(extra.getFirstColumnIndex());
                        mergeCell.setLastColumn(extra.getLastColumnIndex());
                        result.getMergeRegions().add(mergeCell);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    result.setDataList(dataList);
                }
            };

            EasyExcel.read(inputStream).registerReadListener(listener).extraRead(CellExtraTypeEnum.MERGE)
                    .headRowNumber(0) // 无表头
                    .sheet(sheetName)  // 指定 Sheet 名称
                    .doRead();
        } finally {
            // 显式触发垃圾回收，帮助清理临时资源
            System.gc();
        }
        return result;
    }

}