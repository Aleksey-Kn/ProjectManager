package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.request.ProjectRequestDTO;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.services.ProjectService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final JwtProvider provider;

    @PostMapping("/users/project")
    public ResponseEntity<?> addProject(@RequestBody @Valid ProjectRequestDTO requestDTO, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(bindingResult.getAllErrors().get(0), HttpStatus.NOT_ACCEPTABLE);
        } else{
            projectService.addProject(requestDTO, provider.getLoginFromToken());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
