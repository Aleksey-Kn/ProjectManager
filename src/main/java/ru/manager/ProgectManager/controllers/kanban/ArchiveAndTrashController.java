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
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElements;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.services.kanban.ArchiveAndTrashService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/archiver")
@Tag(name = "Средства работы с корзиной и архивом")
public class ArchiveAndTrashController {
    private final ArchiveAndTrashService trashService;
    private final JwtProvider provider;

    @Operation(summary = "Перемещение элемента в архив")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент успешно перемещён"),
            @ApiResponse(responseCode = "403", description = "Польлзователь не имеет доступа к этому проекту"),
            @ApiResponse(responseCode = "400", description = "Элемента с указанным идентификатором не существует",
                    content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "410", description = "Указанный элемент уже в архиве",  content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
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
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Перемещение восстановление элемента обратно в канбан",
            description = "Восстановленеие работает одинаково как из архива, так и из корзины")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент успешно перемещён"),
            @ApiResponse(responseCode = "403", description = "Польлзователь не имеет доступа к этому проекту"),
            @ApiResponse(responseCode = "400", description = "Элемента с указанным идентификатором не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "410", description = "Указанный элемент уже восстановлен",  content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
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
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Получение архива указанного канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элементы, содержащиеся в архиве", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElements.class))
            }),
            @ApiResponse(responseCode = "403", description = "Польлзователь не имеет доступа к этому проекту"),
            @ApiResponse(responseCode = "400", description = "Канбана с указанным идентификатором не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @GetMapping("/archive")
    public ResponseEntity<?> getArchive(@RequestParam @Parameter(description = "Идентификатор канбана") long id,
                                        @RequestParam int pageIndex, @RequestParam int rowCount){
        try {
            Optional<List<KanbanElement>> elements = trashService.findArchive(id, provider.getLoginFromToken());
            if(elements.isPresent()){
                return ResponseEntity.ok(new KanbanElements(elements.get(), pageIndex, rowCount));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Получение корзины указанного канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элементы, содержащиеся в корзине", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElements.class))
            }),
            @ApiResponse(responseCode = "403", description = "Польлзователь не имеет доступа к этому проекту"),
            @ApiResponse(responseCode = "400", description = "Канбана с указанным идентификатором не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @GetMapping("/trash")
    public ResponseEntity<?> getTrash(@RequestParam @Parameter(description = "Идентификатор канбана") long id,
                                      @RequestParam int pageIndex, @RequestParam int rowCount){
        try {
            Optional<List<KanbanElement>> elements = trashService.findTrash(id, provider.getLoginFromToken());
            if(elements.isPresent()){
                return ResponseEntity.ok(new KanbanElements(elements.get(), pageIndex, rowCount));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
        }
    }
}
