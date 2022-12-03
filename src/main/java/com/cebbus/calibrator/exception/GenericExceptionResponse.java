package com.cebbus.calibrator.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class GenericExceptionResponse {
    private final HttpStatus status;
    private final String error;
    private final String message;
    private final String trace;
    private final String detail;
    private final String path;
    private final Boolean success = false;
}
