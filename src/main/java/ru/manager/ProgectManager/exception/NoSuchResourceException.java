package ru.manager.ProgectManager.exception;

public class NoSuchResourceException extends RuntimeException{
    public NoSuchResourceException(String resourceName){
        super("No such resource: " + resourceName);
    }

    public NoSuchResourceException() {
        super("No such specified resource");
    }
}
