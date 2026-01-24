package com.sweet.order.handler;

import com.sweet.common.exception.BaseException;
import com.sweet.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;

@RestControllerAdvice
public class GlobalExceptionHandler implements HandlerInterceptor {

    @ExceptionHandler
    public Result exceptionHandler(BaseException e) {
        return Result.error(e.getMessage());
    }
}
