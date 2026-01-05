package com.gateway.client.service;

import com.gateway.common.utils.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传解析-实现类 接口
 */
public interface IDataProcessingService {

    /**
     * 调用飞书接口
     */
    AjaxResult invokeFeiShu(MultipartFile[] files);

    /**
     * 调用飞书接口 - 带区域参数版本
     */
    AjaxResult invokeFeiShu(MultipartFile[] files, String region);

    /**
     * 返回允许的文件名（原始配置项），用于前端校验与指引
     */
    java.util.List<String> getAllowedFileNames();
}
