package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.NameRequestDTO;
import ru.manager.ProgectManager.DTO.request.PhotoDTO;
import ru.manager.ProgectManager.DTO.response.ProjectResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.services.ProjectService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final JwtProvider provider;

    @PostMapping("/users/project")
    public ResponseEntity<?> addProject(@RequestBody @Valid NameRequestDTO requestDTO, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(bindingResult.getAllErrors().get(0), HttpStatus.NOT_ACCEPTABLE);
        } else{
            return ResponseEntity.ok(projectService.addProject(requestDTO, provider.getLoginFromToken()));
        }
    }

    @GetMapping("/users/project")
    public ResponseEntity<?> findProject(@RequestParam long id){
        Optional<Project> project = projectService.findProject(id);
        if(project.isPresent()){
            return ResponseEntity.ok(new ProjectResponse(project.get()));
        } else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/users/project")
    public ResponseEntity<?> setName(@RequestParam long id, @RequestBody @Valid NameRequestDTO requestDTO,
                                     BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(bindingResult.getAllErrors().get(0), HttpStatus.NOT_ACCEPTABLE);
        } else{
            return ResponseEntity.ok(projectService.setName(id, requestDTO));
        }
    }

    @PostMapping("/users/project/photo")
    public ResponseEntity<?> setPhoto(@RequestParam long id, @ModelAttribute PhotoDTO photoDTO){
        try{
            if(projectService.setPhoto(id, photoDTO.getFile())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>("No such specified project", HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @DeleteMapping("/users/project")
    public ResponseEntity<?> deleteProject(@RequestParam long id){
        try {
            if(projectService.deleteProject(id, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("Not such specified project", HttpStatus.BAD_REQUEST);
        } catch (AssertionError e){
            return new ResponseEntity<>("Not such specified user", HttpStatus.BAD_REQUEST);
        }
    }
}
