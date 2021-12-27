package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.ContentDTO;
import ru.manager.ProgectManager.DTO.response.KanbanResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.KanbanColumn;
import ru.manager.ProgectManager.services.KanbanService;
import ru.manager.ProgectManager.services.ProjectService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class KanbanController {
    private final ProjectService projectService;
    private final KanbanService kanbanService;
    private final JwtProvider provider;

    @GetMapping("/users/kanban/get")
    public ResponseEntity<?> getKanban(@RequestParam long projectId){
        try {
            Optional<List<KanbanColumn>> result = projectService.findKanbans(projectId, provider.getLoginFromToken());
            if(result.isPresent()) {
                return ResponseEntity.ok(new KanbanResponse(result.get()));
            } else{
                return new ResponseEntity<>("The user does not have access to this project", HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified project", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/users/kanban/get_content")
    public ResponseEntity<?> getContent(@RequestParam long elementId){
        try {
            Optional<String> content = kanbanService
                    .getContentFromElement(elementId, provider.getLoginFromToken());
            if(content.isPresent()){
                return ResponseEntity.ok(new ContentDTO(content.get()));
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified element", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/users/kanban/transport_element")
    public ResponseEntity<?> transportElement(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @PutMapping("/users/kanban/transport_column")
    public ResponseEntity<?> transportColumn(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @PutMapping("/users/kanban/element")
    public ResponseEntity<?> editElement(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @PostMapping("/users/kanban/element")
    public ResponseEntity<?> addElement(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @PostMapping("/users/kanban/column")
    public ResponseEntity<?> addColumn(){
        return ResponseEntity.ok("OK"); //TODO
    }

    @DeleteMapping("/users/kanban/element")
    public ResponseEntity<?> removeElement(@RequestParam long elementId){
        try{
            if(kanbanService.deleteElement(elementId, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified element", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/users/kanban/column")
    public ResponseEntity<?> removeColumn(@RequestParam long columnId){
        try{
            if(kanbanService.deleteColumn(columnId, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified element", HttpStatus.BAD_REQUEST);
        }
    }
}
