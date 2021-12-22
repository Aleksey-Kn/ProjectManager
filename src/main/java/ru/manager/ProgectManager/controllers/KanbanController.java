package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.KanbanResponse;
import ru.manager.ProgectManager.services.ProjectService;

@RestController(value = "/user/kanban")
@RequiredArgsConstructor
public class KanbanController {
    private final ProjectService projectService;

    @GetMapping("/get")
    public KanbanResponse getKanban(@RequestParam long projectId){
        return new KanbanResponse(projectService.findKanban(projectId));
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
