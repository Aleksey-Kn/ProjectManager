package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.response.AllUserDataResponse;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.services.UserService;

import java.util.Optional;

@RestController("users/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/get")
    public ResponseEntity<?> findAllData(@RequestParam String login){
        Optional<User> user = userService.findByUsername(login);
        if(user.isPresent()){
            return ResponseEntity.ok(new AllUserDataResponse(user.get()));
        } else{
            return new ResponseEntity<>("No such specified user", HttpStatus.BAD_REQUEST);
        }
    }
}
