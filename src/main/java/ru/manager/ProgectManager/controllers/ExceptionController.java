package ru.manager.ProgectManager.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.exception.ExpiredTokenException;
import ru.manager.ProgectManager.exception.InvalidTokenException;

import java.util.Collections;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> incorrectToken() {
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Token: incorrect token")),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ErrorResponse> expiredToken(){
        return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Token: expired token")),
                HttpStatus.FORBIDDEN);
    }
}
