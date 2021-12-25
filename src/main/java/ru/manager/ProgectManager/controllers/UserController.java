package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.PhotoDTO;
import ru.manager.ProgectManager.DTO.request.RefreshUserDTO;
import ru.manager.ProgectManager.DTO.response.AllUserDataResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.services.UserService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @GetMapping("/users/user")
    public ResponseEntity<?> findAllData(){
        Optional<User> user = userService.findByUsername(jwtProvider.getLoginFromToken());
        if(user.isPresent()){
            return ResponseEntity.ok(new AllUserDataResponse(user.get()));
        } else{
            return new ResponseEntity<>("No such specified user", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("users/user")
    public ResponseEntity<?> refreshMainData(@RequestBody @Valid RefreshUserDTO userDTO, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            StringBuilder stringBuilder = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> stringBuilder.append(e.getDefaultMessage()).append("; "));
            return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.NOT_ACCEPTABLE);
        }
        if(userService.refreshUserData(jwtProvider.getLoginFromToken(), userDTO)){
            return ResponseEntity.ok("All update");
        } else{
            return new ResponseEntity<>("No such specified user", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("users/user/photo")
    public ResponseEntity<?> setPhoto(@RequestBody PhotoDTO photoDTO){
        try {
            if (photoDTO.getFile().getBytes().length > 6_291_456){
                return new ResponseEntity<>("File too big", HttpStatus.NOT_ACCEPTABLE);
            } else {
                if(userService.setPhoto(jwtProvider.getLoginFromToken(), photoDTO.getFile())){
                    return ResponseEntity.ok("OK");
                } else {
                    return new ResponseEntity<>("No such specified user", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (IOException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
