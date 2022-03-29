package ru.manager.ProgectManager.exception;

public class EmailAlreadyUsedException extends RuntimeException{
    public EmailAlreadyUsedException(){
        super("Email already used in other account");
    }
}
