package com.gateway.client.web.exception;

import com.gateway.common.constant.HttpStatus;
import com.gateway.common.exception.ServiceException;
import com.gateway.common.utils.AjaxResult;
import com.gateway.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                          HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        LOG.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 自定义服务器异常错误
     */
    @ExceptionHandler({ServiceException.class})
    public AjaxResult serviceExceptionHandle(ServiceException e) {
        LOG.error("自定义服务器异常:{}", e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 自定义服务器异常错误
     */
    @ExceptionHandler({Exception.class, RuntimeException.class})
    public AjaxResult runtimeExceptionHandle(ServiceException e) {
        LOG.error("服务器异常:{}", e.getMessage(), e);
        return AjaxResult.error(HttpStatus.ERROR, e.getMessage());
    }
}
