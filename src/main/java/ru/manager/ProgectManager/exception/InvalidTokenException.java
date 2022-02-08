package ru.manager.ProgectManager.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(){
        super("Specified token incorrect");
    }
}
