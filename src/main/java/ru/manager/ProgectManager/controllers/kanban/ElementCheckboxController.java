package ru.manager.ProgectManager.controllers.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.request.kanban.CheckboxRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.kanban.CheckBox;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.kanban.KanbanElementAttributesService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/element/checkbox")
public class ElementCheckboxController {
    private final KanbanElementAttributesService attributesService;
    private final JwtProvider provider;

    @PostMapping("/add")
    public ResponseEntity<?> addCheckbox(@RequestBody @Valid CheckboxRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(new ErrorResponse(Errors.TEXT_MUST_BE_CONTAINS_VISIBLE_SYMBOL),
                    HttpStatus.BAD_REQUEST);
        } else{
            try{
                Optional<CheckBox> checkBox = attributesService.addCheckbox(request, provider.getLoginFromToken());
                if(checkBox.isPresent()){
                    return ResponseEntity.ok(checkBox.get());
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e){
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
            }
        }
    }
}
