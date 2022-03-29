package ru.manager.ProgectManager.controllers;

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
import ru.manager.ProgectManager.DTO.request.PhotoDTO;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.KanbanListResponse;
import ru.manager.ProgectManager.DTO.response.ProjectResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.components.PhotoCompressor;
import ru.manager.ProgectManager.entitys.Kanban;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.ProjectService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Создание, изменение и удаление проекта")
public class ProjectController {
    private final ProjectService projectService;
    private final JwtProvider provider;
    private final PhotoCompressor compressor;

    @Operation(summary = "Создание проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "406", description = "Имя проекта не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Возвращается информация о созданном проекте", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponse.class))
            })
    })
    @PostMapping("/project")
    public ResponseEntity<?> addProject(@RequestBody @Valid ProjectDataRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(
                    new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS), HttpStatus.NOT_ACCEPTABLE);
        } else{
            return ResponseEntity.ok(
                    new ProjectResponse(projectService.addProject(request, provider.getLoginFromToken())));
        }
    }

    @Operation(summary = "Получение данных проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником проекта"),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponse.class))
            })
    })
    @GetMapping("/project")
    public ResponseEntity<?> findProject(@RequestParam long id){
        try {
            Optional<Project> project = projectService.findProject(id, provider.getLoginFromToken());
            if (project.isPresent()) {
                return ResponseEntity.ok(new ProjectResponse(project.get()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Изменение проекта", description = "Установление нового имени проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не является администратором проекта"),
            @ApiResponse(responseCode = "406", description = "Имя проекта не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Название проекта изменено")
    })
    @PutMapping("/project")
    public ResponseEntity<?> setData(@RequestParam long id, @RequestBody @Valid ProjectDataRequest request,
                                     BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(
                    new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS), HttpStatus.NOT_ACCEPTABLE);
        } else{
            try {
                if(projectService.setData(id, request, provider.getLoginFromToken()))
                    return new ResponseEntity<>(HttpStatus.OK);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e){
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Добавление картинки проекта", description = "Прикрепление картинки или замена существующей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не является администратором проекта"),
            @ApiResponse(responseCode = "406", description = "Файл не может быть прочитан", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Картинка сжата и сохранена")
    })
    @PostMapping("/project/photo")
    public ResponseEntity<?> setPhoto(@RequestParam long id, @ModelAttribute PhotoDTO photoDTO){
        try{
            if(projectService.setPhoto(id, compressor.compress(photoDTO.getFile()), provider.getLoginFromToken(),
                    photoDTO.getFile().getOriginalFilename())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (IOException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.BAD_FILE),
                    HttpStatus.NOT_ACCEPTABLE);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(
                    new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Получение списка канбанов проекта",
            description = "Отображаются только канбаны, доступные пользователю в соответствии с его ролью")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "200",
                    description = "Список канбан досок в данном проекте, доступных текщему пользователю", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanListResponse.class))
            })
    })
    @GetMapping("/project/kanbans")
    public ResponseEntity<?> allKanbanOfThisUser(@RequestParam @Parameter(description = "Идентификатор проекта")
                                                              long id){
        try {
            Optional<List<Kanban>> kanbans = projectService.findAllKanban(id, provider.getLoginFromToken());
            if (kanbans.isPresent()) {
                return ResponseEntity.ok(new KanbanListResponse(kanbans.get()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Удаление проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не является администратором проекта"),
            @ApiResponse(responseCode = "200", description = "Удаление прошло успешно")
    })
    @DeleteMapping("/project")
    public ResponseEntity<?> deleteProject(@RequestParam long id){
        try {
            if(projectService.deleteProject(id, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
