package ru.manager.ProgectManager.controllers.documents;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.request.documents.CreateSectionRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.documents.SectionService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/users/documents/section")
@RequiredArgsConstructor
@Tag(name = "Манипуляция страницами документации")
public class SectionController {
    private final SectionService sectionService;
    private final JwtProvider provider;

    @Operation(summary = "Добавление страницы документации")
    @PostMapping("/add")
    public ResponseEntity<?> addService(@RequestBody @Valid CreateSectionRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else{
            try{
                Optional<Long> id = sectionService.createSection(request, provider.getLoginFromToken());
                if(id.isPresent()){
                    return ResponseEntity.ok(new IdResponse(id.get()));
                } else{
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e){
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            }
        }
    }
}
