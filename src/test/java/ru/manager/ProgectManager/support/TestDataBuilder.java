package ru.manager.ProgectManager.support;

import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TagRequest;
import ru.manager.ProgectManager.DTO.request.user.AuthDto;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanContentResponse;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponseWithFlag;
import ru.manager.ProgectManager.enums.Locale;

import java.util.List;

public class TestDataBuilder {
    public static RegisterUserDTO buildMasterUserDto() {
        return prepareUser().login("masterUser").email("masterUser@mail.ru").nickname("MasterOfTheGym").build();
    }

    private static RegisterUserDTO.RegisterUserDTOBuilder<?, ?> prepareUser() {
        return RegisterUserDTO.builder().locale(Locale.en).zoneId("+7").password("1234").url("url");
    }

    public static ProjectDataRequest buildProjectDataRequest() {
        return prepareProjectDataRequest().status("Status").name("Project").build();
    }

    public static ProjectDataRequest.ProjectDataRequestBuilder<?, ?> prepareProjectDataRequest() {
        return ProjectDataRequest.builder().deadline("2020-12-30T01:01:30").description("Description")
                .startDate("2000-12-30T01:01:30").statusColor("red");
    }

    public static ProjectResponseWithFlag buildProjectResponseWithFlag(final long id) {
        return prepareProjectResponseWithFlag(id)
                .status("Status")
                .name("Project")
                .roleName("ADMIN")
                .canCreateOrDelete(true)
                .build();
    }

    public static ProjectResponseWithFlag.ProjectResponseWithFlagBuilder<?, ?> prepareProjectResponseWithFlag(final long id) {
        return ProjectResponseWithFlag.builder()
                .deadline("2020-12-30T01:01:30")
                .description("Description")
                .startDate("2000-12-30T01:01:30")
                .status("Status")
                .name("Project")
                .statusColor("red")
                .id(id);
    }

    public static AuthDto buildAuthDto() {
        return AuthDto.builder().browser("Chrome").city("Novosibirsk").ip("10.10.10.10").country("Russia")
                .login("masterUser").password("1234").zoneId("+7").build();
    }

    public static KanbanContentResponse buildKanbanContentResponse(long id) {
        return KanbanContentResponse.builder().id(id).kanbanColumns(List.of()).name("Board").canEdit(true).build();
    }

    public static TagRequest.TagRequestBuilder<?, ?> prepareTagRequest() {
        return TagRequest.builder().color("red");
    }

    public static TagRequest buildTagRequest() {
        return prepareTagRequest().text("Text").build();
    }
}