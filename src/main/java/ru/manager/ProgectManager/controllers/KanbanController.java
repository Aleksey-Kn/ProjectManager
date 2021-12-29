package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.KanbanColumnRequest;
import ru.manager.ProgectManager.DTO.response.ContentDTO;
import ru.manager.ProgectManager.DTO.request.TransportRequest;
import ru.manager.ProgectManager.DTO.response.KanbanResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.KanbanColumn;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.services.KanbanService;
import ru.manager.ProgectManager.services.ProjectService;

import javax.validation.Valid;
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
            Optional<KanbanElement> content = kanbanService
                    .getContentFromElement(elementId, provider.getLoginFromToken());
            if(content.isPresent()){
                return ResponseEntity.ok(new ContentDTO(content.get().getContent(), content.get().getPhoto()));
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified element", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/users/kanban/transport_element")
    public ResponseEntity<?> transportElement(@RequestBody @Valid TransportRequest transportRequest,
                                              BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(bindingResult.getAllErrors().get(0), HttpStatus.NOT_ACCEPTABLE);
        } else {
            try{
                if(kanbanService.transportElement(transportRequest, provider.getLoginFromToken())){
                    return ResponseEntity.ok(
                            new KanbanResponse(kanbanService.findProjectFromElement(transportRequest.getId())
                                    .getKanbanColumns()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e){
                return new ResponseEntity<>("No such specified element", HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    @PutMapping("/users/kanban/transport_column")
    public ResponseEntity<?> transportColumn(@RequestBody @Valid TransportRequest transportRequest,
                                             BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(bindingResult.getAllErrors().get(0), HttpStatus.NOT_ACCEPTABLE);
        } else {
            try{
                if(kanbanService.transportColumn(transportRequest, provider.getLoginFromToken())){
                    return ResponseEntity.ok(
                            new KanbanResponse(kanbanService.findProjectFromColumn(transportRequest.getId())
                                    .getKanbanColumns()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e){
                return new ResponseEntity<>("No such specified column", HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
            }
        }
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
    public ResponseEntity<?> addColumn(@RequestBody KanbanColumnRequest kanbanColumnRequest){
        try {
            if(projectService.addColumn(kanbanColumnRequest, provider.getLoginFromToken()))
                return new ResponseEntity<>(HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified project", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("users/kanban/photo")
    public ResponseEntity<?> addPhoto(){
        return ResponseEntity.ok("OK"); // TODO
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
