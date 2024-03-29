package ru.manager.ProgectManager.controllers.user;

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
import ru.manager.ProgectManager.DTO.UserDetailsDTO;
import ru.manager.ProgectManager.DTO.request.RefreshTokenRequest;
import ru.manager.ProgectManager.DTO.request.user.AuthDto;
import ru.manager.ProgectManager.DTO.request.user.DropPassRequest;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.request.user.ResetPassRequest;
import ru.manager.ProgectManager.DTO.response.AccessTokenResponse;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.user.AuthResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.components.authorization.JwtProvider;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.runtime.EmailAlreadyUsedException;
import ru.manager.ProgectManager.exception.user.AccountIsLockedException;
import ru.manager.ProgectManager.exception.user.AccountIsNotEnabledException;
import ru.manager.ProgectManager.exception.user.IncorrectLoginOrPasswordException;
import ru.manager.ProgectManager.services.RefreshTokenService;
import ru.manager.ProgectManager.services.user.UserService;

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

    @ExceptionHandler(MailException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse maliExceptionHandler() {
        return new ErrorResponse(Errors.NO_SUCH_SPECIFIED_MAIL);
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse emailAlreadyUsedExceptionHandler() {
        return new ErrorResponse(Errors.USER_WITH_THIS_EMAIL_ALREADY_CREATED);
    }

    @ExceptionHandler(DateTimeException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse dateTimeExceptionHandler() {
        return new ErrorResponse(Errors.INCORRECT_TIME_ZONE_FORMAT);
    }

    @ExceptionHandler(AccountIsNotEnabledException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse accountIsNotEnabledExceptionHandler() {
        return new ErrorResponse(Errors.ACCOUNT_IS_NOT_ENABLED);
    }

    @ExceptionHandler(AccountIsLockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse accountIsLockedExceptionHandler() {
        return new ErrorResponse(Errors.ACCOUNT_IS_LOCKED);
    }

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
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserDTO registerUserDTO, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            if (userService.saveUser(registerUserDTO).isPresent()) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ErrorResponse(Errors.USER_WITH_THIS_LOGIN_ALREADY_CREATED),
                        HttpStatus.NOT_ACCEPTABLE);
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
    public ResponseEntity<?> auth(@RequestBody @Valid AuthDto request, BindingResult bindingResult)
            throws IncorrectLoginOrPasswordException, AccountIsNotEnabledException, AccountIsLockedException {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            String username = userService.login(request);
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAccess(jwtProvider.generateToken(username));
            authResponse.setRefresh(refreshTokenService.createToken(username));
            return ResponseEntity.ok(authResponse);
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
            @ApiResponse(responseCode = "400", description = "Рефреш токен не должен быть пустым полем", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshTokenRequest tokenRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            Optional<String> login = refreshTokenService.findLoginAndDropToken(tokenRequest.getRefresh());
            if (login.isPresent()) {
                UserDetailsDTO user = userService.findUserDetailsByUsername(login.get());
                if (user.isAccountNonLocked()) {
                    userService.updateLastVisitAndZone(user.getUsername(), tokenRequest.getZoneId());
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
    }

    @Operation(summary = "Предоставление access токена по refresh токену",
            description = "В отличие от refresh запроса refresh токен при этом не обновляется")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Некорректный refresh токен"),
            @ApiResponse(responseCode = "403", description = "Аккаунт пользователя заблокирован", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Рефреш токен не должен быть пустым полем", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлён", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccessTokenResponse.class))
            })
    })
    @PostMapping("/access")
    public ResponseEntity<?> getNewAccess(@RequestBody @Valid RefreshTokenRequest tokenRequest,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            Optional<String> login = refreshTokenService.findLogin(tokenRequest.getRefresh());
            if (login.isPresent()) {
                UserDetailsDTO user = userService.findUserDetailsByUsername(login.get());
                if (user.isAccountNonLocked()) {
                    userService.updateLastVisitAndZone(user.getUsername(), tokenRequest.getZoneId());
                    AccessTokenResponse tokenResponse = new AccessTokenResponse(jwtProvider.generateToken(login.get()));
                    return ResponseEntity.ok(tokenResponse);
                } else {
                    return new ResponseEntity<>(new ErrorResponse(Errors.ACCOUNT_IS_LOCKED), HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Operation(summary = "Подтверждение почты пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Некорректный токен"),
            @ApiResponse(responseCode = "200", description = "Адрес электронной почты подтверждён", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @GetMapping("/approve")
    public ResponseEntity<?> approve(@RequestParam String token) {
        Optional<String> login = userService.enabledUser(token);
        if (login.isPresent()) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setRefresh(refreshTokenService.createToken(login.get()));
            authResponse.setAccess(jwtProvider.generateToken(login.get()));
            return ResponseEntity.ok(authResponse);
        } else {
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
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            if (userService.attemptDropPass(request.getLoginOrEmail(), request.getUrl())) {
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
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            if (userService.resetPass(request.getToken(), request.getPassword())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
    }
}
