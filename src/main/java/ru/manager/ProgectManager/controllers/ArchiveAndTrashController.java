package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/archive/")
@Tag(name = "Средства работы с корзиной и архивом")
public class ArchiveAndTrashController {

}
