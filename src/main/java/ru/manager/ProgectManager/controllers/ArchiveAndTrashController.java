package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.KanbanElementMainDataResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.services.ArchiveAndTrashService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/archiver")
@Tag(name = "Средства работы с корзиной и архивом")
public class ArchiveAndTrashController {
    private final ArchiveAndTrashService trashService;
    private final JwtProvider provider;

    @PutMapping("/archive")
    public ResponseEntity<?> archive(@RequestParam long id){
        try{
            if(trashService.archive(id, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (IncorrectStatusException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }

    @PutMapping("/reestablish")
    public ResponseEntity<?> reestablish(@RequestParam long id){
        try{
            if(trashService.reestablish(id, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (IncorrectStatusException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }

    @GetMapping("/archive")
    public ResponseEntity<?> getArchive(@RequestParam @Parameter(description = "Идентификатор канбана") long id){
        try {
            Optional<List<KanbanElement>> elements = trashService.findArchive(id, provider.getLoginFromToken());
            if(elements.isPresent()){
                return ResponseEntity.ok(elements.get().stream().map(KanbanElementMainDataResponse::new)
                        .collect(Collectors.toList()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/trash")
    public ResponseEntity<?> getTrash(@RequestParam @Parameter(description = "Идентификатор канбана") long id){
        try {
            Optional<List<KanbanElement>> elements = trashService.findTrash(id, provider.getLoginFromToken());
            if(elements.isPresent()){
                return ResponseEntity.ok(elements.get().stream().map(KanbanElementMainDataResponse::new)
                        .collect(Collectors.toList()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
        }
    }
}
