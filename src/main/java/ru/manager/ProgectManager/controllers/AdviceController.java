package ru.manager.ProgectManager.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.exception.project.NoSuchCustomRoleException;
import ru.manager.ProgectManager.exception.user.IncorrectLoginOrPasswordException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;

import java.io.IOException;

@RestControllerAdvice
public class AdviceController {
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void forbiddenExceptionHandler() {
    }

    @ExceptionHandler(NoSuchProjectException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noSuchProjectExceptionHandler() {
        return new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse ioExceptionHandler() {
        return new ErrorResponse(Errors.BAD_FILE);
    }

    @ExceptionHandler(NoSuchUserException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noSuchUserException() {
        return new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER);
    }

    @ExceptionHandler(IncorrectLoginOrPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse incorrectLoginOrPasswordException() {
        return new ErrorResponse(Errors.INCORRECT_LOGIN_OR_PASSWORD);
    }

    @ExceptionHandler(NoSuchKanbanException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noSuchKanbanExceptionHandler() {
        return new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN);
    }

    @ExceptionHandler(NoSuchCustomRoleException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noSuchCustomRoleExceptionHandler() {
        return new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE);
    }
}
