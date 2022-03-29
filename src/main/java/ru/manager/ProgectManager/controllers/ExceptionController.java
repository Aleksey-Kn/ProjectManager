package ru.manager.ProgectManager.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.ExpiredTokenException;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ErrorResponse> expiredToken(){
        return new ResponseEntity<>(new ErrorResponse(Errors.TOKEN_EXPIRED),
                HttpStatus.UNAUTHORIZED);
    }
}
