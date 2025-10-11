package com.babgo.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorType {
    HttpStatus getHttpStatus();
    String getMessage();
}
