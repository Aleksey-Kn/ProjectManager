package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.KanbanResponse;
import ru.manager.ProgectManager.services.ProjectService;

import java.util.NoSuchElementException;

@RestController(value = "/user/kanban")
@RequiredArgsConstructor
public class KanbanController {
    private final ProjectService projectService;

    @GetMapping("/get")
    public ResponseEntity<?> getKanban(@RequestParam long projectId){
        try {
            return ResponseEntity.ok(new KanbanResponse(projectService.findKanbans(projectId)));
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified project", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get_content")
    public String getContent(@RequestParam long elementId){
        return null; //TODO
    }

    @PutMapping("/transport_element")
    public ResponseEntity<?> transportElement(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @PutMapping("/transport_column")
    public ResponseEntity<?> transportColumn(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @PutMapping("/element")
    public ResponseEntity<?> editElement(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @PostMapping("/element")
    public ResponseEntity<?> addElement(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @PostMapping("/column")
    public ResponseEntity<?> addColumn(){
        return ResponseEntity.ok("OK"); //TODO
    }
}
