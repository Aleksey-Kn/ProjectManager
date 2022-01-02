package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.services.AccessService;


@RestController
@RequiredArgsConstructor
public class AccessController {
    private final AccessService accessService;
    private final JwtProvider provider;

    @Value("${server.port}")
    private String port;

    @GetMapping("users/access")
    public ResponseEntity<?> getAccess(@RequestParam String token){
        if(accessService.createAccessForUser(token, provider.getLoginFromToken())){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("No such specified token", HttpStatus.BAD_REQUEST);
    }

    private String configureLink(String value){
        return String.format("https://localhost:%s/users/access?token=%s", port, value);
    }
}
