package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.request.RefreshTokenRequest;
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
public class AuthController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

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
