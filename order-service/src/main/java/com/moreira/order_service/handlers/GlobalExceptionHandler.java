package com.moreira.order_service.handlers;

import com.moreira.order_service.controller.OrderController;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice(assignableTypes = OrderController.class)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException(@NonNull IllegalArgumentException illegalArgumentException) {
        return new ResponseEntity<>(new ErrorMessage(illegalArgumentException.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAccessDeniedException(@NonNull AccessDeniedException accessDeniedException) {
        return new ResponseEntity<>(new ErrorMessage(accessDeniedException.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorMessage> handleAccessDeniedException(@NonNull NoSuchElementException noSuchElementException) {
        return new ResponseEntity<>(new ErrorMessage(noSuchElementException.getMessage()), HttpStatus.NOT_FOUND);
    }

}
