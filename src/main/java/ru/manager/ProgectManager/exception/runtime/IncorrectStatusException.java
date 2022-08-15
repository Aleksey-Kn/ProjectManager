package ru.manager.ProgectManager.exception.runtime;

public class IncorrectStatusException extends RuntimeException{
    public IncorrectStatusException(){
        super("Incorrect status for this action");
    }
}
