package ru.manager.ProgectManager.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.manager.ProgectManager.exception.InvalidTokenException;

@RestControllerAdvice
public class ExceptionController {

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Token incorrect or deprecated")
    @ExceptionHandler(InvalidTokenException.class)
    public void incorrectToken() {
    }
}
