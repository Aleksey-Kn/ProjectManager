package ru.manager.ProgectManager.components;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.enums.Errors;

import java.util.stream.Collectors;

@Component
public class ErrorResponseEntityConfigurator {
    public ResponseEntity<?> createErrorResponse(BindingResult bindingResult) {
        return new ResponseEntity<>(
                new ErrorResponse(bindingResult.getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .map(Errors::valueOf)
                        .map(Errors::getNumValue)
                        .collect(Collectors.toList())),
                HttpStatus.BAD_REQUEST);
    }
}
