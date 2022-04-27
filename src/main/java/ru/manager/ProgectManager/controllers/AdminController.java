package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.AdminService;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/lock")
    public ResponseEntity<?> lock(@RequestParam @Parameter(description = "Идентификатор блокируемого пользователя")
                                              long id) {
        try {
            if(adminService.lockAccount(id)) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> unlock(@RequestParam @Parameter(description = "Идентификатор разблокируемого пользователя")
                                                long id) {
        if(adminService.unlockAccount(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
        }
    }
}
