package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.AuthResponse;
import ru.manager.ProgectManager.DTO.UserDTO;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.services.UserService;

import javax.validation.Valid;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            if (userService.saveUser(userDTO)) {
                return ResponseEntity.ok("OK");
            } else {
                return new ResponseEntity<>("Пользователь с таким именем уже существует", HttpStatus.BAD_REQUEST);
            }
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> stringBuilder.append(e.getDefaultMessage()).append("; "));
            return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping("/auth")
    public ResponseEntity<?> auth(@RequestBody UserDTO request) {
        try {
            User userEntity = userService.findByUsernameAndPassword(request.getUsername(), request.getPassword()).orElseThrow();
            String token = jwtProvider.generateToken(userEntity.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Некорректный логин или пароль", HttpStatus.UNAUTHORIZED);
        }
    }
}
