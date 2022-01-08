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
import ru.manager.ProgectManager.DTO.response.AccessProjectResponse;
import ru.manager.ProgectManager.DTO.response.AuthResponse;
import ru.manager.ProgectManager.DTO.request.UserDTO;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.services.RefreshTokenService;
import ru.manager.ProgectManager.services.UserService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Контроллер аутентификации", description = "Здесь выполняется регистрация, авторизация и обновление токенов")
public class AuthController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Регистрация")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Попытка регистрации пользователя с существующим логином",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "406", description = "Некорректные значения полей", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Возвращаются токены доступа", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            })
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            Optional<User> userOptional = userService.saveUser(userDTO);
            if (userOptional.isPresent()) {
                AuthResponse authResponse = new AuthResponse();
                authResponse.setAccess(jwtProvider.generateToken(userOptional.get().getUsername()));
                authResponse.setRefresh(refreshTokenService.createToken(userOptional.get().getUsername()));
                return ResponseEntity.ok(authResponse);
            } else {
                return new ResponseEntity<>(
                        new ErrorResponse(Collections.singletonList("Login: User with this login already created")),
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
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
    public ResponseEntity<?> auth(@RequestBody UserDTO request) {
        try {
            User userEntity = userService.findByUsernameOrEmailAndPassword(request.getLogin(), request.getPassword()).orElseThrow();
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAccess(jwtProvider.generateToken(userEntity.getUsername()));
            authResponse.setRefresh(refreshTokenService.createToken(userEntity.getUsername()));
            return ResponseEntity.ok(authResponse);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("User: Incorrect login or password")),
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
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest tokenRequest){
        Optional<String> login = refreshTokenService.findLoginFromToken(tokenRequest.getRefresh());
        if(login.isPresent()){
            AuthResponse authResponse = new AuthResponse();
            authResponse.setRefresh(refreshTokenService.createToken(login.get()));
            authResponse.setAccess(jwtProvider.generateToken(login.get()));
            return ResponseEntity.ok(authResponse);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
