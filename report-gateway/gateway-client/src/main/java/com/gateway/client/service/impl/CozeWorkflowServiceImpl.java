package com.gateway.client.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.coze.openapi.client.workflows.run.RunWorkflowReq;
import com.coze.openapi.client.workflows.run.RunWorkflowResp;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import com.gateway.client.config.properties.CozeAiConfig;
import com.gateway.client.service.ICozeWorkflowService;
import com.gateway.common.utils.AjaxResult;
import com.gateway.common.utils.DateUtils;
import com.gateway.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CozeWorkflowServiceImpl implements ICozeWorkflowService {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(CozeWorkflowServiceImpl.class);

    private static final int SUCCESS_CODE = 0;

    @Autowired
    private CozeAiConfig cozeAiConfig;


    @Override
    public AjaxResult callCozeWorkflow(String region) {
        LOG.info("callCozeWorkflow, cozeAiConfig: {}", JSON.toJSONString(cozeAiConfig));
        TokenAuth authCli = new TokenAuth(cozeAiConfig.getSecretToken());
        CozeAPI coze = new CozeAPI.Builder()
                .baseURL(cozeAiConfig.getUrl())
                .auth(authCli)
                .connectTimeout(cozeAiConfig.getConnectTimeout())
                .readTimeout(cozeAiConfig.getReadTimeout())
                .build();
        Map<String, Object> paraMap = new HashMap<>(5);
        paraMap.put("daqu", region);
        Date startTime = DateUtils.addDays(new Date(), -cozeAiConfig.getTimeSpan());
        paraMap.put("start", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, startTime));
        paraMap.put("end", DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD));
        LOG.info("callCozeWorkflow, paraMap: {}", JSON.toJSONString(paraMap));

        RunWorkflowReq.RunWorkflowReqBuilder builder = RunWorkflowReq.builder();
        builder.workflowID(cozeAiConfig.getWorkflowId()).appID(cozeAiConfig.getAppId()).parameters(paraMap);
        RunWorkflowResp resp = coze.workflows().runs().create(builder.build());
        if (resp == null) {
            LOG.info("callCozeWorkflow, resp is null");
            return AjaxResult.error("调用Coze工作流失败:resp is null");
        }
        LOG.info("callCozeWorkflow, response successfully.");
        if (null == resp.getCode() || SUCCESS_CODE != resp.getCode().intValue()) {
            LOG.info("callCozeWorkflow, code is {}", resp.getCode());
            return AjaxResult.error("调用Coze工作流失败:code is " + (null == resp.getCode() ? "null" : resp.getCode().intValue()));
        }
        String dataJson = resp.getData();
        if (StringUtils.isEmpty(dataJson)) {
            LOG.info("callCozeWorkflow, data is null");
            return AjaxResult.error("调用Coze工作流失败:data is null");
        }
        JSONObject dataObject = JSON.parseObject(dataJson);
        if (dataObject == null) {
            LOG.info("callCozeWorkflow, dataObject is null");
            return AjaxResult.error("调用Coze工作流失败: dataObject is null");
        }
        String output = dataObject.getString("data");
        if (StringUtils.isBlank(output)) {
            LOG.info("callCozeWorkflow, output is blank");
            return AjaxResult.error("调用Coze工作流失败:output is blank");
        }
        AjaxResult result = AjaxResult.success();
//        JSONObject outputObject = null;
//        AjaxResult result = AjaxResult.success();
//        try {
//            int firstBrace = output.indexOf('{');
//            int lastValidIndex = output.lastIndexOf('}');
//            String validOutput = output.substring(firstBrace, lastValidIndex + 1);
//            outputObject = JSON.parseObject(validOutput);
//        } catch (Exception e) {
//            LOG.info("callCozeWorkflow, output is not json, output: {}", output, e);
//            result = AjaxResult.error("调用Coze工作流失败:output is not json");
//        }
//        if (!result.isSuccess()) {
//            return result;
//        }
        if (output.trim().isEmpty()) {
            LOG.info("callCozeWorkflow end failed");
        } else {
            LOG.info("callCozeWorkflow end successfully.");
        }
        result.put(AjaxResult.DATA_TAG, output);
        LOG.info("callCozeWorkflow end successfully.");
        return result;
    }
}
