package com.sweet.user.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;
import sweet.shop.common.exception.BaseException;
import sweet.shop.common.result.Result;

@RestControllerAdvice
public class GlobalExceptionHandler implements HandlerInterceptor {

    @ExceptionHandler
    public Result exceptionHandler(BaseException e) {
        return Result.error(e.getMessage());
    }
}
