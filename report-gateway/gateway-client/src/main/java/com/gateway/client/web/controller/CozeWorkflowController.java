package com.gateway.client.web.controller;

import com.gateway.client.service.ICozeWorkflowService;
import com.gateway.client.service.TaskStatusManager;
import com.gateway.common.utils.AjaxResult;
import com.gateway.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class CozeWorkflowController {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(CozeWorkflowController.class);

    @Autowired
    private ICozeWorkflowService cozeWorkflowService;

    @Autowired
    private TaskStatusManager taskStatusManager;

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
        } finally {
            // 无论成功还是失败，都清除任务状态
            taskStatusManager.completeTask();
            LOG.info("postCozeWorkflow:任务状态已清除");
        }
        LOG.info("postCozeWorkflow:调用Coze工作流结束, successfully.");
        return ajaxResult;
    }

    @GetMapping("/api/markdown")
    public ResponseEntity<String> getMarkdown(@RequestParam("file") String file) {
        if (file == null || file.trim().isEmpty() || !file.matches("^[a-zA-Z0-9._-]+$")) {
            return ResponseEntity.badRequest().body("invalid file name");
        }
        Path path = Paths.get("/tmp", file);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return ResponseEntity.status(404).body("file not found");
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes, StandardCharsets.UTF_8);
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(content);
        } catch (IOException e) {
            LOG.error("getMarkdown failed", e);
            return ResponseEntity.status(500).body("failed to read file");
        }
    }
}
