package com.gateway.client.web.controller;

import com.gateway.client.service.ICozeWorkflowService;
import com.gateway.common.utils.AjaxResult;
import com.gateway.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CozeWorkflowController {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(CozeWorkflowController.class);

    @Autowired
    private ICozeWorkflowService cozeWorkflowService;

    @GetMapping("/api/validate")
    public AjaxResult validate() {
        return AjaxResult.success("The system is running!!!!");
    }

    // @CrossOrigin(origins = "*", maxAge = 3600)
    @PostMapping("/api/generate-report")
    public AjaxResult postCozeWorkflow(HttpServletRequest request) {
        String region = request.getParameter("region");
        if (StringUtils.isBlank(region)) {
            LOG.info("postCozeWorkflow:必填自动【区域】不能为空");
            return AjaxResult.error("必填自动【区域】不能为空");
        }
        AjaxResult ajaxResult;
        try {
            ajaxResult = cozeWorkflowService.callCozeWorkflow(region);
        } catch (Exception e) {
            LOG.error("postCozeWorkflow:调用Coze工作流失败", e);
            ajaxResult = AjaxResult.error("调用Coze工作流失败");
        }
        LOG.info("postCozeWorkflow:调用Coze工作流结束, successfully.");
        return ajaxResult;
    }
}
