package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.adminAction.LockRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.user.UserDataForAdminList;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.AdminService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Возможности суперпользователя")
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;

    @Operation(summary = "Блокирование пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно заблокирован"),
            @ApiResponse(responseCode = "400", description = "Поля запроса не должны быть пустыми", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Нельзя блокировать суперпользователей"),
            @ApiResponse(responseCode = "404", description = "Указанного пользователя не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/lock")
    public ResponseEntity<?> lock(@RequestBody @Valid LockRequest lockRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.LOGIN_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (adminService.lockAccount(lockRequest)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Разблокирование пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно разблокирован"),
            @ApiResponse(responseCode = "404", description = "Указанного пользователя не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/unlock")
    public ResponseEntity<?> unlock(@RequestParam @Parameter(description = "Идентификатор разблокируемого пользователя")
                                            String idOrLogin) {
        if (adminService.unlockAccount(idOrLogin)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение данных всех пользователей системы")
    @ApiResponse(responseCode = "200", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserDataForAdminList.class))
    })
    @GetMapping("/all")
    public UserDataForAdminList allUser(Principal principal) {
        return adminService.findAllUser(userService.findZoneIdForThisUser(principal.getName()));
    }
}
