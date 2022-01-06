package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.PhotoDTO;
import ru.manager.ProgectManager.DTO.request.RefreshUserDTO;
import ru.manager.ProgectManager.DTO.response.AllUserDataResponse;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.PublicUserDataResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.components.PhotoCompressor;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.services.UserService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final PhotoCompressor compressor;

    @GetMapping("/users/user")
    public ResponseEntity<?> findAllData(@RequestParam long id) {
        Optional<User> user;
        if (id == -1) { //about yourself
            user = userService.findByUsername(jwtProvider.getLoginFromToken());
            if (user.isPresent()) {
                return ResponseEntity.ok(new AllUserDataResponse(user.get()));
            } else {
                return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("User: No such specified user")),
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            user = userService.findById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(new PublicUserDataResponse(user.get()));
            } else {
                return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("User: No such specified user")),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PutMapping("users/user")
    public ResponseEntity<?> refreshMainData(@RequestBody @Valid RefreshUserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        }
        userService.refreshUserData(jwtProvider.getLoginFromToken(), userDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("users/user/photo")
    public ResponseEntity<?> setPhoto(@ModelAttribute PhotoDTO photoDTO) {
        try {
            userService.setPhoto(jwtProvider.getLoginFromToken(), compressor.compress(photoDTO.getFile()));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(e.getMessage())),
                    HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
