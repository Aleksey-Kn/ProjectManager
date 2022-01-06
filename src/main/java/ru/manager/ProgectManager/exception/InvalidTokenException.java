package ru.manager.ProgectManager.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String token){
        super("Specified token incorrect: " + token);
    }
}
