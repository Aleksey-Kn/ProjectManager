package ru.manager.ProgectManager.controllers;

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
import ru.manager.ProgectManager.DTO.request.PhotoDTO;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.ProjectResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.components.PhotoCompressor;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.ProjectService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> addProject(@RequestBody @Valid NameRequest requestDTO, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(new ErrorResponse(bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .map(Errors::valueOf)
                    .map(Errors::getNumValue)
                    .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else{
            return ResponseEntity.ok(
                    new ProjectResponse(projectService.addProject(requestDTO, provider.getLoginFromToken())));
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
    public ResponseEntity<?> setName(@RequestParam long id, @RequestBody @Valid NameRequest requestDTO,
                                     BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(new ErrorResponse(bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .map(Errors::valueOf)
                    .map(Errors::getNumValue)
                    .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else{
            try {
                if(projectService.setName(id, requestDTO, provider.getLoginFromToken()))
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
            if(projectService.setPhoto(id, compressor.compress(photoDTO.getFile()), provider.getLoginFromToken())) {
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
