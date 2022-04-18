package ru.manager.ProgectManager.services.documents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.documents.CreateSectionRequest;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PageRepository pageRepository;
    private final CustomRoleWithDocumentConnectorRepository documentConnectorRepository;
    private  final CustomProjectRoleRepository roleRepository;

    public Optional<Long> createPage(CreateSectionRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(request.getProjectId()).get();
        if (canEditResource(project, user)) {
            Page page = new Page();
            page.setContent(request.getContent());
            page.setName(request.getName());
            page.setProject(project);
            Optional<Page> parent = pageRepository.findById(request.getParentId());
            if(parent.isPresent()){
                page.setParent(parent.get());
                page.setRoot(parent.get().getRoot() == null? parent.get(): parent.get().getRoot());
            } else {
                page.setRoot(null);
                page.setParent(null);
            }
            page = pageRepository.save(page);

            project.getPages().add(page);
            projectRepository.save(project);
            return Optional.of(page.getId());
        } else {
            return Optional.empty();
        }
    }

    public boolean deletePage(long id, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        Page page = pageRepository.findById(id).get();
        Project project = page.getProject();
        if(canEditResource(project, user) && canEditPage(page, user)){
            if(page.getParent() != null){
                Page parent = page.getParent();
                parent.getSubpages().remove(page);
                pageRepository.save(parent);
            } else {
                project.getAvailableRole().parallelStream()
                        .filter(r -> r.getCustomRoleWithDocumentConnectors().parallelStream()
                                .anyMatch(connector -> connector.getPage().equals(page)))
                        .forEach(r -> {
                            Set<CustomRoleWithDocumentConnector> forRemove = r.getCustomRoleWithDocumentConnectors()
                                    .parallelStream()
                                    .filter(connector -> connector.getPage().equals(page))
                                    .collect(Collectors.toSet());
                            r.getCustomRoleWithDocumentConnectors().removeAll(forRemove);
                            roleRepository.save(r);
                            documentConnectorRepository.deleteAll(forRemove);
                        });
            }
            pageRepository.delete(page);
            return true;
        } else{
            return false;
        }
    }

    public boolean rename(long id, String name, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Page page = pageRepository.findById(id).get();
        if(canEditPage(page, user)){
            page.setName(name);
            pageRepository.save(page);
            return true;
        } else{
            return false;
        }
    }

    public boolean setContent(long id, String content, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Page page = pageRepository.findById(id).get();
        if(canEditPage(page, user)){
            page.setContent(content);
            pageRepository.save(page);
            return true;
        } else {
            return false;
        }
    }

    public Optional<Page> find(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Page page = pageRepository.findById(id).get();
        if(canSeePage(page, user)){
            return Optional.of(page);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Set<Page>> findAllRoot(long projectId, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).get();
        Optional<UserWithProjectConnector> withProjectConnector = project.getConnectors().stream()
                .filter(c -> c.getUser().equals(user)).findAny();
        if(withProjectConnector.isPresent()) {
            if(withProjectConnector.get().getRoleType() == TypeRoleProject.CUSTOM_ROLE) {
                return Optional.of(withProjectConnector.get().getCustomProjectRole().getCustomRoleWithDocumentConnectors()
                        .parallelStream()
                        .map(CustomRoleWithDocumentConnector::getPage)
                        .collect(Collectors.toSet()));
            } else {
                return Optional.of(project.getPages().parallelStream()
                        .filter(p -> p.getRoot() == null)
                        .collect(Collectors.toSet()));
            }
        } else {
            return Optional.empty();
        }
    }

    private boolean canEditPage(Page page, User user) {
        Page root = (page.getRoot() == null ? page : page.getRoot());
        return page.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithDocumentConnectors().stream()
                .filter(CustomRoleWithDocumentConnector::isCanEdit)
                .anyMatch(connector -> connector.getPage().equals(root))));
    }

    private boolean canSeePage(Page page, User user){
        Page root = (page.getRoot() == null ? page : page.getRoot());
        return page.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithDocumentConnectors().stream()
                .anyMatch(connector -> connector.getPage().equals(root))));
    }

    private boolean canEditResource(Project project, User user) {
        return project.getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() == TypeRoleProject.ADMIN || c.getCustomProjectRole().isCanEditResources()));
    }
}
