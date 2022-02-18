package ru.manager.ProgectManager.exception;

public class IncorrectStatusException extends RuntimeException{
    public IncorrectStatusException(){
        super("Incorrect status for this action");
    }
}
