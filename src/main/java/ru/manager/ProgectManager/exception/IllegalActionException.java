package ru.manager.ProgectManager.exception;

public class IllegalActionException extends RuntimeException {
    public IllegalActionException() {
        super("This action is illegal");
    }
}
