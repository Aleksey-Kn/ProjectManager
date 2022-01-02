package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.AccessRequest;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.services.AccessService;

import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class AccessController {
    private final AccessService accessService;
    private final JwtProvider provider;

    @Value("${server.port}")
    private String port;

    @GetMapping("/users/access")
    public ResponseEntity<?> getAccess(@RequestParam String token){
        try {
            if (accessService.createAccessForUser(token, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified token", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/users/access")
    public ResponseEntity<String> postAccess(@RequestBody AccessRequest accessRequest){
        try{
            Optional<String> token = accessService.generateTokenForAccessProject(provider.getLoginFromToken(),
                    accessRequest.getProjectId(), accessRequest.isHasAdmin(), accessRequest.isDisposable(),
                    accessRequest.getLiveTimeInDays());
            return token.map(s -> ResponseEntity.ok(configureLink(s)))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.FORBIDDEN));
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified project", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private String configureLink(String value){
        return String.format("https://localhost:%s/users/access?token=%s", port, value);
    }
}
