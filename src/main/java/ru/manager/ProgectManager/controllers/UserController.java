package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.manager.ProgectManager.DTO.response.AllUserDataResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.services.UserService;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public static String getBearerTokenHeader() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest()
                .getHeader("Authorization")
                .substring(7);
    }

    @GetMapping("/users/user/get")
    public ResponseEntity<?> findAllData(){
        String token = getBearerTokenHeader();
        String login = jwtProvider.getLoginFromToken(token);
        Optional<User> user = userService.findByUsername(login);
        if(user.isPresent()){
            return ResponseEntity.ok(new AllUserDataResponse(user.get()));
        } else{
            return new ResponseEntity<>("No such specified user", HttpStatus.BAD_REQUEST);
        }
    }
}
