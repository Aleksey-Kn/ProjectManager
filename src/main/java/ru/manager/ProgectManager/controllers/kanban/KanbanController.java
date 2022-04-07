package ru.manager.ProgectManager.controllers.kanban;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.DTO.request.kanban.GetKanbanRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TagRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanResponse;
import ru.manager.ProgectManager.DTO.response.kanban.TagListResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.AccessProjectService;
import ru.manager.ProgectManager.services.kanban.KanbanService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban")
@Tag(name = "Манипуляции с канбан-доской")
public class KanbanController {
    private final KanbanService kanbanService;
    private final AccessProjectService accessProjectService;
    private final JwtProvider provider;

    @Operation(summary = "Добавление новой канбан-доски в проект")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Указанного проекта не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному действию"),
            @ApiResponse(responseCode = "406", description = "Имя не должно быть пустым",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Kanban.class))
            })
    })
    @PostMapping("/new")
    public ResponseEntity<?> createKanban(@RequestParam long projectId, @RequestBody @Valid NameRequest name,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                Optional<Kanban> kanban =
                        kanbanService.createKanban(projectId, name.getName(), provider.getLoginFromToken());
                if (kanban.isPresent()) {
                    return ResponseEntity.ok(kanban.get());
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Получение канбан-доски",
            description = "Получение всего канбана, кроме контента элементов колонок")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Указанного канбана не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "406", description = "Указаны некорректные индекс или количество элементов",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному ресурсу"),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @GetMapping("/get")
    public ResponseEntity<?> getKanban(@RequestBody @Valid GetKanbanRequest kanbanRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                String login = provider.getLoginFromToken();
                Optional<Kanban> result = kanbanService.findKanban(kanbanRequest.getId(), login);
                if (result.isPresent()) {
                    return ResponseEntity.ok(new KanbanResponse(result.get(),
                            kanbanRequest.getPageColumnIndex(), kanbanRequest.getCountColumn(),
                            kanbanRequest.getPageElementIndex(), kanbanRequest.getCountElement(),
                            accessProjectService.canEditKanban(kanbanRequest.getId(), login)));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Удаление всей канбан-доски")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему канбану", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Канбан доска успешно удалена")
    })
    @DeleteMapping("/all_kanban")
    public ResponseEntity<?> removeKanban(@RequestParam long id) {
        try {
            if (kanbanService.removeKanban(id, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Получаение всех доступных для этого канбана тегов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему канбану", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Список тегов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TagListResponse.class))
            })
    })
    @GetMapping("/tags")
    public ResponseEntity<?> findAllTags(@RequestParam long id){
        try{
            Optional<Set<ru.manager.ProgectManager.entitys.kanban.Tag>> tags =
                    kanbanService.findAllAvailableTags(id, provider.getLoginFromToken());
            if(tags.isPresent()){
                return ResponseEntity.ok(new TagListResponse(tags.get()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Добавление тега в канбан")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему канбану", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Информация о добавленном теге", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ru.manager.ProgectManager.entitys.kanban.Tag.class))
            })
    })
    @PostMapping("/tag")
    public ResponseEntity<?> addTag(@RequestParam long id, @RequestBody TagRequest request){
        try {
            Optional<ru.manager.ProgectManager.entitys.kanban.Tag> tag =
                    kanbanService.addTag(id, request, provider.getLoginFromToken());
            if(tag.isPresent()){
                return ResponseEntity.ok(tag.get());
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Удаление тега из канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему канбану или тегу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Тег успешно удалён из канбана")
    })
    @DeleteMapping("/tag")
    public ResponseEntity<?> deleteTag(@RequestParam long kanbanId, @RequestParam long tagId){
        try{
            if(kanbanService.removeTag(kanbanId, tagId, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_TAG), HttpStatus.BAD_REQUEST);
        }
    }
}
