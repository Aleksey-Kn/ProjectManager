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
import ru.manager.ProgectManager.DTO.request.RefreshTokenRequest;
import ru.manager.ProgectManager.DTO.request.user.AuthDto;
import ru.manager.ProgectManager.DTO.request.user.DropPassRequest;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.request.user.ResetPassRequest;
import ru.manager.ProgectManager.DTO.response.AuthResponse;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.components.authorization.JwtProvider;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.EmailAlreadyUsedException;
import ru.manager.ProgectManager.services.RefreshTokenService;
import ru.manager.ProgectManager.services.UserService;

import javax.validation.Valid;
import java.time.DateTimeException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authorization")
@Tag(name = "Регистрация, авторизация, обновление токенов, смена пароля и подтверждение почты")
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
            @ApiResponse(responseCode = "400", description = "Необходимые данные отсутствуют в запросе",
                    content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "401", description = "Некорректный логин или пароль", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Аккаунт пользователя заблокирован", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "406", description = "Неприемлемые данные чаового пояса", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @PostMapping("/login")
    public ResponseEntity<?> auth(@RequestBody @Valid AuthDto request, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return entityConfigurator.createErrorResponse(bindingResult);
            } else {
                Optional<User> userEntity = userService.login(request);
                if (userEntity.isPresent()) {
                    if (userEntity.get().isEnabled()) {
                        if (userEntity.get().isAccountNonLocked()) {
                            AuthResponse authResponse = new AuthResponse();
                            authResponse.setAccess(jwtProvider.generateToken(userEntity.get().getUsername()));
                            authResponse.setRefresh(refreshTokenService.createToken(userEntity.get().getUsername()));
                            return ResponseEntity.ok(authResponse);
                        } else {
                            return new ResponseEntity<>(new ErrorResponse(Errors.ACCOUNT_IS_LOCKED), HttpStatus.FORBIDDEN);
                        }
                    } else {
                        return new ResponseEntity<>(new ErrorResponse(Errors.ACCOUNT_IS_NOT_ENABLED),
                                HttpStatus.UNAUTHORIZED);
                    }
                } else {
                    return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_LOGIN_OR_PASSWORD),
                            HttpStatus.UNAUTHORIZED);
                }
            }
        } catch (DateTimeException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_TIME_ZONE_FORMAT),
                    HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @Operation(summary = "Обновление токенов",
            description = "Возвращает новые токены доступа по refresh-токену пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Некорректный refresh токен"),
            @ApiResponse(responseCode = "403", description = "Аккаунт пользователя заблокирован", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest tokenRequest) {
        Optional<String> login = refreshTokenService.findLoginAndDropToken(tokenRequest.getRefresh());
        if (login.isPresent()) {
            if(userService.findByUsername(login.get()).orElseThrow().isAccountNonLocked()) {
                AuthResponse authResponse = new AuthResponse();
                authResponse.setRefresh(refreshTokenService.createToken(login.get()));
                authResponse.setAccess(jwtProvider.generateToken(login.get()));
                return ResponseEntity.ok(authResponse);
            } else {
                return new ResponseEntity<>(new ErrorResponse(Errors.ACCOUNT_IS_LOCKED), HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Подтверждение почты пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Некорректный токен"),
            @ApiResponse(responseCode = "200", description = "Адрес электронной почты подтверждён")
    })
    @GetMapping("/approve")
    public ResponseEntity<?> approve(@RequestParam String token) {
        if(userService.enabledUser(token)){
            return new ResponseEntity<>(HttpStatus.OK);
        } else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "Запрос на сброс пароля",
            description = "Пользователю с указанным логином или почтой присылается письмо со ссылкой, по которой можно сбросить пароль")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Поле логина не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Ссылка для сброса пароля отправлена на почту"),
            @ApiResponse(responseCode = "404",
                    description = "Аккаунта, соответствующего введённым данным, не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/drop")
    public ResponseEntity<?> dropPass(@RequestBody @Valid DropPassRequest request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            if(userService.attemptDropPass(request.getLoginOrEmail(), request.getUrl())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Сброс пароля",
            description = "Производится исходя из корректности данных, отправленных на почту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Неприемлемый пароль", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменён"),
            @ApiResponse(responseCode = "403", description = "Некорректный токен")
    })
    @PostMapping("/reset")
    public ResponseEntity<?> resetPass(@RequestBody @Valid ResetPassRequest request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            if(userService.resetPass(request.getToken(), request.getPassword())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
    }
}
