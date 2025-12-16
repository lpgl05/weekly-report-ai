package com.gateway.client.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.client.config.properties.FeishuConfig;
import com.gateway.client.domain.FeiShuSheet;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.okhttp.*;
import com.lark.oapi.service.sheets.v3.model.QuerySpreadsheetSheetReq;
import com.lark.oapi.service.sheets.v3.model.QuerySpreadsheetSheetResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 数据处理服务实现类-调用飞书接口 工具类
 */
@Service
public class DataProcessingFeiShuServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(DataProcessingFeiShuServiceImpl.class);

    /**
     * 飞书配置
     */
    @Autowired
    private FeishuConfig feishuConfig;

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * 获取飞书Token
     */
    public String getFeiShuAccessToken() {
        log.info("processing-调用飞书接口-获取飞书Token-开始");
        String url = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("app_id", feishuConfig.getAppId());
        requestBody.put("app_secret", feishuConfig.getAppSecret());

        try {
            String responseStr = executeRequest(url, null, requestBody, "POST");
            JSONObject result = JSONObject.parseObject(responseStr);
            if (result.getIntValue("code") != 0) {
                log.error("processing-调用飞书接口-获取飞书Token-异常,code={},msg={}", result.getIntValue("code"),
                        result.getString("msg"));
                return "";
            }
            String token = result.getString("tenant_access_token");
            log.info("processing-调用飞书接口-获取飞书Token-成功,token={}", token);
            return token;
        } catch (Exception e) {
            log.error("processing-调用飞书接口-获取飞书Token-异常", e);
            return "";
        }
    }

    /**
     * 获取飞书表格Sheet
     */
    public Map<String, FeiShuSheet> getFeishuSheetMap(String spreadsheetToken) throws Exception {
        log.info("processing-调用飞书接口-获取飞书表格Sheet-开始");
        Map<String, FeiShuSheet> sheetMap = new HashMap<>();
        Client client = Client.newBuilder(feishuConfig.getAppId(), feishuConfig.getAppSecret()).build();
        QuerySpreadsheetSheetReq req = QuerySpreadsheetSheetReq.newBuilder().spreadsheetToken(spreadsheetToken).build();
        QuerySpreadsheetSheetResp resp = client.sheets().v3().spreadsheetSheet().query(req);
        if (!resp.success()) {
            log.info("processing-调用飞书接口-获取飞书表格Sheet-失败,resp={}", JSON.toJSONString(resp));
            return sheetMap;
        }
        JSONObject json = JSONObject.parseObject(Jsons.DEFAULT.toJson(resp.getData()));
        JSONArray sheets = json.getJSONArray("sheets");
        for (int i = 0; i < sheets.size(); i++) {
            JSONObject sheet = sheets.getJSONObject(i);
            FeiShuSheet feiShuSheet = new FeiShuSheet();
            String sheetName = sheet.getString("title"); // Sheet 名称
            feiShuSheet.setTitle(sheetName);
            feiShuSheet.setSheetId(sheet.getString("sheet_id"));
            JSONObject properties = sheet.getJSONObject("grid_properties");
            feiShuSheet.setFrozenRowCount(properties.getInteger("frozen_row_count"));
            feiShuSheet.setFrozenColumnCount(properties.getInteger("frozen_column_count"));
            feiShuSheet.setRowCount(properties.getInteger("row_count"));
            feiShuSheet.setColumnCount(properties.getInteger("column_count"));
            sheetMap.put(sheetName, feiShuSheet);
        }
        log.info("processing-调用飞书接口-获取飞书表格Sheet-结束");
        return sheetMap;
    }

    /**
     * 新增行/列
     */
    public boolean addRow(String tableToken, String sheetToken, String accessToken, int length, String type) {
        log.info("processing-调用飞书接口-新增行-开始");
        String url = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/" + tableToken + "/dimension_range";
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> dimension = new HashMap<>();
        dimension.put("sheetId", sheetToken);
        dimension.put("majorDimension", type);
        dimension.put("length", length);
        requestBody.put("dimension", dimension);
        log.info("processing-调用飞书接口-新增行-请求={}", requestBody);
        String post = executeRequest(url, accessToken, requestBody, "POST");
        log.info("processing-调用飞书接口-新增行-返回={}", post);
        return checkResponse(post);
    }

    /**
     * 删除行/列
     */
    public boolean deleteRow(String tableToken, String sheetToken, String accessToken, int start, int end,
            String type) {
        String url = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/" + tableToken + "/dimension_range";
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> dimension = new HashMap<>();
        dimension.put("sheetId", sheetToken);
        dimension.put("majorDimension", type);
        dimension.put("startIndex", start);
        dimension.put("endIndex", end);
        requestBody.put("dimension", dimension);
        log.info("processing-调用飞书接口-删除行-请求={}", requestBody);
        String post = executeRequest(url, accessToken, requestBody, "DELETE");
        log.info("processing-调用飞书接口-删除行-返回={}", post);
        return checkResponse(post);
    }

    /**
     * 更新行
     */
    public boolean updateRow(String tableToken, String accessToken, Map<String, Object> valueRange) {
        String url = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/" + tableToken + "/values";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("valueRange", valueRange);
        log.info("processing-调用飞书接口-更新行-请求");
        String post = executeRequest(url, accessToken, requestBody, "PUT");
        log.info("processing-调用飞书接口-更新行-返回");
        return checkResponse(post);
    }

    /**
     * 合并行
     */
    public boolean mergeRow(String tableToken, String accessToken, String valueRange) {
        String url = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/" + tableToken + "/merge_cells";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("range", valueRange); // 合并的单元格范围
        requestBody.put("mergeType", "MERGE_ALL");  // 合并类型
        log.info("processing-调用飞书接口-合并行-请求={}", requestBody);
        String post = executeRequest(url, accessToken, requestBody, "POST");
        log.info("processing-调用飞书接口-合并行-返回={}", requestBody);
        return checkResponse(post);
    }

    private String executeRequest(String url, String accessToken, Map<String, Object> requestBody, String method) {
        // 统一的JSON序列化
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("请求参数序列化失败", e);
        }

        // 构建请求
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json");

        switch (method.toUpperCase()) {
            case "POST":
                builder.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody));
                break;
            case "DELETE":
                builder.delete(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody));
                break;
            case "PUT":
                builder.put(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody));
                break;
        }

        // 统一的响应处理
        try (Response response = OK_HTTP_CLIENT.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败 [code=" + response.code() + "]: " + response.message());
            }
            return response.body() != null ? response.body().string() : "";
        } catch (IOException e) {
            throw new RuntimeException("API调用异常", e);
        }
    }

    /**
     * 检查飞书接口响应结果
     */
    private boolean checkResponse(String response) {
        try {
            if (response == null || response.isEmpty()) {
                return false;
            }
            JSONObject jsonObject = JSONObject.parseObject(response);
            return jsonObject.getIntValue("code") == 0;
        } catch (Exception e) {
            log.error("解析飞书接口响应失败, response={}", response, e);
            return false;
        }
    }

}