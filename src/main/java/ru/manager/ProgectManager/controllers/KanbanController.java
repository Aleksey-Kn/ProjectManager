package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.*;
import ru.manager.ProgectManager.DTO.response.ContentDTO;
import ru.manager.ProgectManager.DTO.response.KanbanResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.components.PhotoCompressor;
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
    private final PhotoCompressor compressor;

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

    @GetMapping("/users/kanban/element")
    public ResponseEntity<?> getContent(@RequestParam long elementId){
        try {
            Optional<KanbanElement> content = kanbanService
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
    public ResponseEntity<?> transportElement(@RequestBody @Valid TransportRequest transportRequest,
                                              BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(bindingResult.getAllErrors().get(0), HttpStatus.NOT_ACCEPTABLE);
        } else {
            try{
                if(kanbanService.transportElement(transportRequest, provider.getLoginFromToken())){
                    return ResponseEntity.ok(new KanbanResponse(projectService
                                    .findKanbans(kanbanService.findProjectFromElement(transportRequest.getId()).getId(),
                                            provider.getLoginFromToken()).get()));
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
            String login = provider.getLoginFromToken();
            try{
                if(kanbanService.transportColumn(transportRequest, login)){
                    return ResponseEntity.ok(new KanbanResponse(projectService
                            .findKanbans(kanbanService
                                    .findProjectFromColumn(transportRequest.getId()).getId(), login).get()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e){
                return new ResponseEntity<>("No such specified column", HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    @PostMapping("/users/kanban/element")
    public ResponseEntity<?> editElement(@RequestBody @Valid CreateKanbanElementRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(bindingResult.getAllErrors().get(0), HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                String login = provider.getLoginFromToken();
                if (kanbanService.addElement(request, login)) {
                    return ResponseEntity.ok(new KanbanResponse(projectService
                            .findKanbans(kanbanService.findProjectFromColumn(request.getColumnId()).getId(), login)
                            .get()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e){
                return new ResponseEntity<>("No such specified column", HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PutMapping("/users/kanban/element")
    public ResponseEntity<?> addElement(@RequestParam long id, @RequestBody @Valid UpdateKanbanElementRequest request,
                                        BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(bindingResult.getAllErrors().get(0), HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                String login = provider.getLoginFromToken();
                if (kanbanService.setElement(id, request, login)) {
                    return ResponseEntity.ok(new KanbanResponse(projectService
                            .findKanbans(kanbanService.findProjectFromElement(id).getId(), login)
                            .get()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e){
                return new ResponseEntity<>("No such specified column or element", HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PostMapping("/users/kanban/column")
    public ResponseEntity<?> addColumn(@RequestBody @Valid KanbanColumnRequest kanbanColumnRequest,
                                       BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            StringBuilder stringBuilder = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> stringBuilder.append(e.getDefaultMessage()).append("; "));
            return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            String login = provider.getLoginFromToken();
            if(projectService.addColumn(kanbanColumnRequest, login))
                return ResponseEntity.ok(new KanbanResponse(projectService
                        .findKanbans(kanbanColumnRequest.getProjectId(), login).get()));
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified project", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/users/kanban/column")
    public ResponseEntity<?> renameColumn(@RequestParam long id, @RequestBody @Valid NameRequestDTO name,
                                          BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            StringBuilder stringBuilder = new StringBuilder();
            bindingResult.getAllErrors().forEach(e -> stringBuilder.append(e.getDefaultMessage()).append("; "));
            return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            String login = provider.getLoginFromToken();
            if(kanbanService.renameColumn(id, name.getName(), login))
                return ResponseEntity.ok(new KanbanResponse(projectService
                        .findKanbans(kanbanService.findProjectFromColumn(id).getId(), login)
                        .get()));
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified project", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("users/kanban/photo")
    public ResponseEntity<?> addPhoto(@RequestParam long id, @ModelAttribute PhotoDTO photoDTO){
        try {
            if(kanbanService.setPhoto(id, provider.getLoginFromToken(), compressor.compress(photoDTO.getFile()))){
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified element", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/users/kanban/element")
    public ResponseEntity<?> removeElement(@RequestParam long id){
        try{
            String login = provider.getLoginFromToken();
            if(kanbanService.deleteElement(id, login)){
                return ResponseEntity.ok(new KanbanResponse(projectService
                        .findKanbans(kanbanService.findProjectFromElement(id).getId(), login).get()));
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified element", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/users/kanban/column")
    public ResponseEntity<?> removeColumn(@RequestParam long id){
        try{
            String login = provider.getLoginFromToken();
            if(kanbanService.deleteColumn(id, login)){
                return ResponseEntity.ok(new KanbanResponse(projectService
                        .findKanbans(kanbanService.findProjectFromColumn(id).getId(), login).get()));
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>("No such specified element", HttpStatus.BAD_REQUEST);
        }
    }
}
