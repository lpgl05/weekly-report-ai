package com.gateway.client.web.controller;

import com.gateway.client.service.IDataProcessingService;
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

    /**
     * 调用飞书接口
     */
    // @CrossOrigin(origins = "*", maxAge = 3600)
    @PostMapping("/api/upload")
    public AjaxResult invokeFeiShu(@RequestParam("files") MultipartFile[] files) {
        log.info("processing-调用飞书接口-开始");
        return dataProcessingService.invokeFeiShu(files);
    }

    /**
     * 返回允许的文件名列表，便于前端在上传前进行校验与提示
     */
    @GetMapping("/api/upload/allowed")
    public AjaxResult allowedFileNames() {
        return AjaxResult.success(dataProcessingService.getAllowedFileNames());
    }
}
