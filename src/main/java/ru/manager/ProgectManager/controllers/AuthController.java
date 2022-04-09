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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.request.RefreshTokenRequest;
import ru.manager.ProgectManager.DTO.request.UserDTO;
import ru.manager.ProgectManager.DTO.response.AuthResponse;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.EmailAlreadyUsedException;
import ru.manager.ProgectManager.services.RefreshTokenService;
import ru.manager.ProgectManager.services.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Здесь выполняется регистрация, авторизация и обновление токенов")
public class AuthController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

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
            @ApiResponse(responseCode = "200", description = "Возвращаются токены доступа", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult,
                                          HttpServletResponse response) {
        if (!bindingResult.hasErrors()) {
            try {
                Optional<User> userOptional = userService.saveUser(userDTO);
                if (userOptional.isPresent()) {
                    AuthResponse authResponse = new AuthResponse();
                    authResponse.setRefresh(refreshTokenService.createToken(userOptional.get().getUsername()));

                    Cookie cookie = new Cookie("access", jwtProvider.generateToken(userOptional.get().getUsername()));
                    cookie.setPath("/");
                    cookie.setMaxAge(900);
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);//добавляем Cookie в запрос
                    response.setContentType("text/plain");//устанавливаем контент

                    return ResponseEntity.ok(authResponse);
                } else {
                    return new ResponseEntity<>(
                            new ErrorResponse(Errors.USER_WITH_THIS_LOGIN_ALREADY_CREATED), HttpStatus.NOT_ACCEPTABLE);
                }
            } catch (EmailAlreadyUsedException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.USER_WITH_THIS_EMAIL_ALREADY_CREATED), HttpStatus.NOT_ACCEPTABLE);
            }
        } else {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Аутентификация")
    @ApiResponses(value = {
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
    public ResponseEntity<?> auth(@RequestBody UserDTO request, HttpServletResponse response) {
        try {
            User userEntity = userService.findByUsernameOrEmailAndPassword(request.getLogin(), request.getPassword()).orElseThrow();
            AuthResponse authResponse = new AuthResponse();

            Cookie cookie = new Cookie("access", jwtProvider.generateToken(userEntity.getUsername()));
            cookie.setPath("/");
            cookie.setMaxAge(900);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);//добавляем Cookie в запрос
            response.setContentType("text/plain");//устанавливаем контент

            authResponse.setRefresh(refreshTokenService.createToken(userEntity.getUsername()));
            return ResponseEntity.ok(authResponse);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_LOGIN_OR_PASSWORD),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Обновление токенов",
            description = "Возвращает новые токены доступа по refresh-токену пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Некорректный refresh токен"),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest tokenRequest, HttpServletResponse response) {
        Optional<String> login = refreshTokenService.findLoginFromToken(tokenRequest.getRefresh());
        if (login.isPresent()) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setRefresh(refreshTokenService.createToken(login.get()));

            Cookie cookie = new Cookie("access", jwtProvider.generateToken(login.get()));
            cookie.setPath("/");
            cookie.setMaxAge(900);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);//добавляем Cookie в запрос
            response.setContentType("text/plain");//устанавливаем контекст

            return ResponseEntity.ok(authResponse);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
