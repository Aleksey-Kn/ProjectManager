package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.AuthDto;
import ru.manager.ProgectManager.DTO.request.RefreshTokenRequest;
import ru.manager.ProgectManager.DTO.request.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.response.AuthResponse;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.EmailAlreadyUsedException;
import ru.manager.ProgectManager.services.RefreshTokenService;
import ru.manager.ProgectManager.services.UserService;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "Регистрация, авторизация и обновление токенов")
public class AuthController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final ErrorResponseEntityConfigurator entityConfigurator;

    @Operation(summary = "Регистрация")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "406",
                    description = "Попытка регистрации пользователя с существующим логином или почтой",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Некорректные значения полей", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Указанной почты не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегестрирован")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserDTO registerUserDTO, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            try {
                if (userService.saveUser(registerUserDTO)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(
                            new ErrorResponse(Errors.USER_WITH_THIS_LOGIN_ALREADY_CREATED), HttpStatus.NOT_ACCEPTABLE);
                }
            } catch (EmailAlreadyUsedException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.USER_WITH_THIS_EMAIL_ALREADY_CREATED), HttpStatus.NOT_ACCEPTABLE);
            } catch (MailException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_MAIL), HttpStatus.NOT_FOUND);
            }
        } else {
            return entityConfigurator.createErrorResponse(bindingResult);
        }
    }

    @Operation(summary = "Авторизация")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Данные логина или пароля отсутствуют в запросе",
                    content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "401", description = "Некорректный логин или пароль", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @PostMapping("/auth")
    public ResponseEntity<?> auth(@RequestBody @Valid AuthDto request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            Optional<User> userEntity = userService
                    .findByUsernameOrEmailAndPassword(request.getLogin(), request.getPassword());
            if (userEntity.isPresent()) {
                if (userEntity.get().isEnabled()) {
                    AuthResponse authResponse = new AuthResponse();
                    authResponse.setAccess(jwtProvider.generateToken(userEntity.get().getUsername()));
                    authResponse.setRefresh(refreshTokenService.createToken(userEntity.get().getUsername()));
                    return ResponseEntity.ok(authResponse);
                } else {
                    return new ResponseEntity<>(new ErrorResponse(Errors.ACCOUNT_IS_NOT_ENABLED),
                            HttpStatus.UNAUTHORIZED);
                }
            } else {
                return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_LOGIN_OR_PASSWORD),
                        HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Operation(summary = "Обновление токенов",
            description = "Возвращает новые токены доступа по refresh-токену пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Некорректный refresh токен"),
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest tokenRequest) {
        Optional<String> login = refreshTokenService.findLoginFromToken(tokenRequest.getRefresh());
        if (login.isPresent()) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setRefresh(refreshTokenService.createToken(login.get()));
            authResponse.setAccess(jwtProvider.generateToken(login.get()));
            return ResponseEntity.ok(authResponse);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Operation(summary = "Подтверждение почты пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Некорректный токен"),
            @ApiResponse(responseCode = "200", description = "Адрес электронной почты подтверждён")
    })
    @GetMapping("/approve")
    public ResponseEntity<?> approve(@RequestParam String token) {
        if(userService.enabledUser(token)){
            return new ResponseEntity<>(HttpStatus.OK);
        } else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
