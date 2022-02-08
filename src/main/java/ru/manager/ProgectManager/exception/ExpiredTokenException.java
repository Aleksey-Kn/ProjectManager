package ru.manager.ProgectManager.exception;

public class ExpiredTokenException extends RuntimeException{
    public ExpiredTokenException(){
        super("Specified token expired");
    }
}
