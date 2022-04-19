package ru.manager.ProgectManager.controllers.documents;

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
import ru.manager.ProgectManager.DTO.request.ContentRequest;
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.DTO.request.documents.CreateSectionRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.documents.PageService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/users/documents")
@RequiredArgsConstructor
@Tag(name = "Манипуляция страницами документации")
public class DocumentController {
    private final PageService pageService;
    private final JwtProvider provider;

    @Operation(summary = "Добавление страницы документации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Идентификатор созданного документа", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/add")
    public ResponseEntity<?> addService(@RequestBody @Valid CreateSectionRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                Optional<Long> id = pageService.createPage(request, provider.getLoginFromToken());
                if (id.isPresent()) {
                    return ResponseEntity.ok(new IdResponse(id.get()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            }
        }
    }

    @PutMapping("/rename")
    public ResponseEntity<?> rename(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                    @RequestBody @Valid NameRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (pageService.rename(id, request.getName(), provider.getLoginFromToken())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
            }
        }
    }

    @PutMapping("/content")
    public ResponseEntity<?> setContent(@RequestBody ContentRequest request) {
        try {
            if (pageService.setContent(request.getId(), request.getContent(), provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/publish")
    public ResponseEntity<?> publish(@RequestParam @Parameter(description = "Идентификатор страницы") long id) {
        try {
            if (pageService.publish(id, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
        }
    }
}
