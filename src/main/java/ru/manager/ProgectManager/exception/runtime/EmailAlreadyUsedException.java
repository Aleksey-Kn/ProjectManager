package ru.manager.ProgectManager.exception.runtime;

public class EmailAlreadyUsedException extends RuntimeException{
    public EmailAlreadyUsedException(){
        super("Email already used in other account");
    }
}
