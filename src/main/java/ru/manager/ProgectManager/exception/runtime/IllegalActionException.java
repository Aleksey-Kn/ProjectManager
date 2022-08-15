package ru.manager.ProgectManager.exception.runtime;

public class IllegalActionException extends RuntimeException {
    public IllegalActionException() {
        super("This action is illegal");
    }
}
