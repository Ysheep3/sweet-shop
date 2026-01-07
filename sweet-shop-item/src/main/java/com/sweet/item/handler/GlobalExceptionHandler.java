package com.sweet.item.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;
import com.sweet.common.exception.BaseException;
import com.sweet.common.result.Result;

@RestControllerAdvice
public class GlobalExceptionHandler implements HandlerInterceptor {

    @ExceptionHandler
    public Result exceptionHandler(BaseException e) {
        return Result.error(e.getMessage());
    }
}
