package ru.manager.ProgectManager.support;

import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.enums.Locale;

public class TestDataBuilder {
    public static RegisterUserDTO buildMasterUserDto() {
        return prepareUser().login("masterUser").email("masterUser@mail.ru").nickname("MasterOfTheGym").build();
    }

    private static RegisterUserDTO.RegisterUserDTOBuilder<?, ?> prepareUser() {
        return RegisterUserDTO.builder().locale(Locale.en).zoneId("+7").password("1234").url("url");
    }

    public static ProjectDataRequest buildProjectDto() {
        return prepareProjectRequest().status("Status").name("Project").build();
    }

    public static ProjectDataRequest.ProjectDataRequestBuilder<?, ?> prepareProjectRequest() {
        return ProjectDataRequest.builder().deadline("2020-12-30T01:01:30").description("Description")
                .startDate("2000-12-30T01:01:30").statusColor("red");
    }

    public static Project buildProject(final long id) {
        Project project = new Project();
        project.setDeadline("2020-12-30T01:01:30");
        project.setDescription("Description");
        project.setStartDate("2000-12-30T01:01:30");
        project.setStatus("Status");
        project.setName("Project");
        project.setStatusColor("red");
        project.setId(id);
        return project;
    }
}