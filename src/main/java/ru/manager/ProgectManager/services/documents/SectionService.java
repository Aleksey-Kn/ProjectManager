package ru.manager.ProgectManager.services.documents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.documents.CreateSectionRequest;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.SectionRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SectionRepository sectionRepository;

    public Optional<Long> createSection(CreateSectionRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(request.getProjectId()).get();
        if (canEditResource(project, user)) {
            Page page = new Page();
            page.setContent(request.getContent());
            page.setName(request.getName());
            page.setProject(project);
            page.setRoot(null);
            page = sectionRepository.save(page);

            project.getPages().add(page);
            projectRepository.save(project);
            return Optional.of(page.getId());
        } else {
            return Optional.empty();
        }
    }

//    public boolean deleteSection(long id, String userLogin){
//        User user = userRepository.findByUsername(userLogin);
//
//    }

    private boolean canEditPage(Page page, User user) {
        Page root = (page.getRoot() == null ? page : page.getRoot());
        return page.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithDocumentConnectors().stream()
                .filter(CustomRoleWithDocumentConnector::isCanEdit)
                .anyMatch(connector -> connector.getPage().equals(root))));
    }

    private boolean canEditResource(Project project, User user) {
        return project.getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() == TypeRoleProject.ADMIN || c.getCustomProjectRole().isCanEditResources()));
    }
}
