package com.cebbus.calibrator.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
@RestController
public class GenericExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({BadCredentialsException.class, AuthenticationServiceException.class})
    public final ResponseEntity<GenericExceptionResponse> handleBadCredentialException(RuntimeException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.FORBIDDEN;

        return new ResponseEntity<>(GenericExceptionResponse.builder()
                .status(status)
                .build(), status);
    }

    @ExceptionHandler({Exception.class})
    public final ResponseEntity<GenericExceptionResponse> handleException(RuntimeException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(GenericExceptionResponse.builder()
                .status(status)
                .build(), status);
    }

}
