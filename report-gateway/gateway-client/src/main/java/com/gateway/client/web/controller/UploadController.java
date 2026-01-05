package com.gateway.client.web.controller;

import com.gateway.client.service.IDataProcessingService;
import com.gateway.client.service.TaskStatusManager;
import com.gateway.common.utils.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传解析-控制器
 */
@RestController
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private IDataProcessingService dataProcessingService;

    @Autowired
    private TaskStatusManager taskStatusManager;

    /**
     * 调用飞书接口
     */
    // @CrossOrigin(origins = "*", maxAge = 3600)
    @PostMapping("/api/upload")
    public AjaxResult invokeFeiShu(
        @RequestParam("files") MultipartFile[] files,
        @RequestParam(value = "region", required = false, defaultValue = "未知区域") String region
    ) {
        log.info("processing-调用飞书接口-开始, region={}", region);
        return dataProcessingService.invokeFeiShu(files, region);
    }

    /**
     * 返回允许的文件名列表，便于前端在上传前进行校验与提示
     */
    @GetMapping("/api/upload/allowed")
    public AjaxResult allowedFileNames() {
        return AjaxResult.success(dataProcessingService.getAllowedFileNames());
    }

    /**
     * 检查是否有任务正在执行
     * 前端在上传前调用此接口，如果有任务在执行则提示用户稍后再试
     */
    @GetMapping("/api/task/status")
    public AjaxResult checkTaskStatus() {
        log.info("checking-task-status");
        boolean isRunning = taskStatusManager.isTaskRunning();
        String region = taskStatusManager.getCurrentTaskRegion();
        long elapsedSeconds = taskStatusManager.getTaskElapsedSeconds();
        
        AjaxResult result = new AjaxResult();
        result.put("isRunning", isRunning);
        if (isRunning) {
            result.put("message", "有用户正在处理" + region + "的数据，请稍候再试");
            result.put("region", region);
            result.put("elapsedSeconds", elapsedSeconds);
        }
        return result;
    }
    }
}
