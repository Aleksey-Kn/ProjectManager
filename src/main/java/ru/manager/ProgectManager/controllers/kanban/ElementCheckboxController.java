package ru.manager.ProgectManager.controllers.kanban;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.DTO.request.kanban.CheckboxRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.components.authorization.JwtProvider;
import ru.manager.ProgectManager.entitys.kanban.CheckBox;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.services.kanban.KanbanElementAttributesService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/element/checkbox")
@Tag(name = "Манипуляции с чекбоксами элементов канбан-доски")
public class ElementCheckboxController {
    private final KanbanElementAttributesService attributesService;
    private final JwtProvider provider;

    @Operation(summary = "Добавление нового чекбокса в элемент канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Идентификатор добавленного чекбокса", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            }),
            @ApiResponse(responseCode = "403",
                    description = "Данный пользователь не имеет прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "400", description = "Текст чекбокса должен содержать видимые символы", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Указанного элемента канбана не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "410", description = "Элемент перемещён в корзину и недоступен для изменеия",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @PostMapping()
    public ResponseEntity<?> addCheckbox(@RequestBody @Valid CheckboxRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(new ErrorResponse(Errors.TEXT_MUST_BE_CONTAINS_VISIBLE_SYMBOL),
                    HttpStatus.BAD_REQUEST);
        } else{
            try{
                Optional<CheckBox> checkBox = attributesService.addCheckbox(request, provider.getLoginFromToken());
                if(checkBox.isPresent()){
                    return ResponseEntity.ok(new IdResponse(checkBox.get().getId()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e){
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
            } catch (IncorrectStatusException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                        HttpStatus.GONE);
            }
        }
    }

    @Operation(summary = "Удаление чекбокса из элемента канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Удаление чекбокса произошло успешно"),
            @ApiResponse(responseCode = "403",
                    description = "Данный пользователь не имеет прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного чекбокса не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "410", description = "Элемент перемещён в корзину и недоступен для изменеия",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @DeleteMapping()
    public ResponseEntity<?> removeCheckbox(@RequestParam @Parameter(description = "Идентификатор чекбокса") long id){
        try{
            if(attributesService.deleteCheckbox(id, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CHECKBOX), HttpStatus.NOT_FOUND);
        } catch (IncorrectStatusException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }

    @Operation(summary = "Изменение значения чекбокса на противоположное")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изменение произошло успешно"),
            @ApiResponse(responseCode = "403",
                    description = "Данный пользователь не имеет прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного чекбокса не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "410", description = "Элемент перемещён в корзину и недоступен для изменеия",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @PutMapping("/select")
    public ResponseEntity<?> click(@RequestParam @Parameter(description = "Идентификатор чекбокса") long id){
        try{
            if(attributesService.tapCheckbox(id, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CHECKBOX), HttpStatus.NOT_FOUND);
        } catch (IncorrectStatusException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }

    @Operation(summary = "Изменение текстового поля чекбокса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изменение произошло успешно"),
            @ApiResponse(responseCode = "403",
                    description = "Данный пользователь не имеет прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного чекбокса не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "410", description = "Элемент перемещён в корзину и недоступен для изменеия",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @PutMapping("/rename")
    public ResponseEntity<?> rename(@RequestParam @Parameter(description = "Идентификатор чекбокса") long id,
                                    @RequestBody @Valid NameRequest nameRequest){
        try{
            if(attributesService.editCheckbox(id, nameRequest.getName(), provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CHECKBOX), HttpStatus.NOT_FOUND);
        } catch (IncorrectStatusException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }
}
