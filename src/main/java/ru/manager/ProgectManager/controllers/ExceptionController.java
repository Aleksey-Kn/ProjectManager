package ru.manager.ProgectManager.controllers;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;

import java.util.Collections;

@RestControllerAdvice
public class ExceptionController {

    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    @ExceptionHandler({SignatureException.class, UnsupportedJwtException.class, MalformedJwtException.class})
    public ErrorResponse incorrectToken() {
        return new ErrorResponse(Collections.singletonList("Access token: incorrect access token"));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ErrorResponse deprecatedToken(){
        return new ErrorResponse(Collections.singletonList("Access token: is deprecated"));
    }
}
