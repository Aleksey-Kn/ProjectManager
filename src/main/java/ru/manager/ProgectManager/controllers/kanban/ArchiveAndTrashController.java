package ru.manager.ProgectManager.controllers.kanban;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElementMainDataResponse;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.IncorrectElementStatusException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanElementException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.services.kanban.ArchiveAndTrashService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/archiver")
@Tag(name = "Средства работы с корзиной и архивом")
public class ArchiveAndTrashController {
    private final ArchiveAndTrashService trashService;

    @Operation(summary = "Перемещение элемента в архив")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент успешно перемещён"),
            @ApiResponse(responseCode = "403", description = "Польлзователь не имеет доступа к этому проекту"),
            @ApiResponse(responseCode = "404", description = "Элемента с указанным идентификатором не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "410", description = "Указанный элемент уже в архиве", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/archive")
    public void archive(@RequestParam long id, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        trashService.archive(id, principal.getName());
    }

    @Operation(summary = "Перемещение восстановление элемента обратно в канбан",
            description = "Восстановленеие работает одинаково как из архива, так и из корзины")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент успешно перемещён"),
            @ApiResponse(responseCode = "403", description = "Польлзователь не имеет доступа к этому проекту"),
            @ApiResponse(responseCode = "404", description = "Элемента с указанным идентификатором не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "410", description = "Указанный элемент уже восстановлен", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/reestablish")
    public void reestablish(@RequestParam long id, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        trashService.reestablish(id, principal.getName());
    }

    @Operation(summary = "Получение архива указанного канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элементы, содержащиеся в архиве", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementMainDataResponse[].class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к этому проекту"),
            @ApiResponse(responseCode = "404", description = "Канбана с указанным идентификатором не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @GetMapping("/archive")
    public KanbanElementMainDataResponse[] getArchive(@RequestParam @Parameter(description = "Идентификатор канбана") long id,
                                                      @RequestParam int pageIndex, @RequestParam int rowCount,
                                                      Principal principal)
            throws ForbiddenException, NoSuchKanbanException {
        return trashService.findArchive(id, principal.getName()).stream()
                .skip(pageIndex)
                .limit(rowCount)
                .toArray(KanbanElementMainDataResponse[]::new);
    }

    @Operation(summary = "Получение корзины указанного канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элементы, содержащиеся в корзине", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementMainDataResponse[].class))
            }),
            @ApiResponse(responseCode = "403", description = "Польлзователь не имеет доступа к этому проекту"),
            @ApiResponse(responseCode = "404", description = "Канбана с указанным идентификатором не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @GetMapping("/trash")
    public KanbanElementMainDataResponse[] getTrash(@RequestParam @Parameter(description = "Идентификатор канбана") long id,
                                      @RequestParam int pageIndex, @RequestParam int rowCount, Principal principal)
            throws ForbiddenException, NoSuchKanbanException {
        return trashService.findTrash(id, principal.getName()).stream()
                .skip(pageIndex)
                .limit(rowCount)
                .toArray(KanbanElementMainDataResponse[]::new);
    }
}
