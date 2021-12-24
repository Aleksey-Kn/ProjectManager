package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.response.AllUserDataResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.services.UserService;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @GetMapping("/users/user/get")
    public ResponseEntity<?> findAllData(){
        Optional<User> user = userService.findByUsername(jwtProvider.getLoginFromToken());
        if(user.isPresent()){
            return ResponseEntity.ok(new AllUserDataResponse(user.get()));
        } else{
            return new ResponseEntity<>("No such specified user", HttpStatus.BAD_REQUEST);
        }
    }
}
