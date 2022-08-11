package ru.manager.ProgectManager.support;

import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponseWithFlag;
import ru.manager.ProgectManager.enums.Locale;

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
}