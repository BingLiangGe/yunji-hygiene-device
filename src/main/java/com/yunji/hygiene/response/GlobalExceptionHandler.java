package com.yunji.hygiene.response;


import com.yunji.hygiene.entity.domain.DeviceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @version 1.0
 * @Project yunji-cabinet-mall
 * @Package com.yunji.handler
 * @FileName GlobalExceptionHandler.java
 * @Copyright Copyright © 2024 云集互联. All Rights Reserved
 * Company		com
 * @CreateTime 2024-07-29 11:05:43
 * @Description 全局异常处理器
 * @Modification Author                    History Time			Version				Description
 * ----------------------------------------------------------------------------------
 * xiaozhang0803@163.com	2020-12-25 09:34:31		1.0					通道表
 * @since JDK 1.8.0_202
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logs = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Resource
    HttpServletRequest request;

    /**
     * 业务异常
     */
    @ExceptionHandler(DeviceException.class)
    public Response<?> handleServiceException(DeviceException e) {
        logs.error("请求地址'{}',发生设备异常:{}", request.getRequestURI(), e.getMessage());
        return ResponseHelper.failure(e.getCode(), e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Response<?> handleException(Exception e) {
        String requestURI = request.getRequestURI();
        logs.error("请求地址'{}',发生系统异常.", requestURI, e);
        return ResponseHelper.failure(-1, "系统异常,请查看日志信息");
    }
}
