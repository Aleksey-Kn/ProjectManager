package ru.manager.ProgectManager.services.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.entitys.user.VisitMark;
import ru.manager.ProgectManager.enums.ResourceType;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.VisitMarkRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitMarkUpdater {
    private final VisitMarkRepository visitMarkRepository;
    private final UserRepository userRepository;

    public void updateVisitMarks(User user, Page page){
        Optional<VisitMark> visitMark = user.getVisitMarks().stream()
                .filter(v -> v.getResourceType() == ResourceType.DOCUMENT
                        && v.getResourceId() == page.getId()).findAny();
        if(visitMark.isPresent()) {
            transportMark(user, visitMark.get());
        } else {
            VisitMark newMark = generateDefaultVisitMark(user, ResourceType.DOCUMENT, page.getId(), page.getName());
            newMark.setProjectId(page.getProject().getId());
            newMark.setProjectName(page.getProject().getName());
            user.getVisitMarks().add(visitMarkRepository.save(newMark));
            userRepository.save(user);
        }
    }

    public void updateVisitMarks(User user, Kanban kanban){
        Optional<VisitMark> visitMark = user.getVisitMarks().stream()
                .filter(v -> v.getResourceType() == ResourceType.KANBAN
                        && v.getResourceId() == kanban.getId()).findAny();
        if(visitMark.isPresent()) {
            transportMark(user, visitMark.get());
        } else {
            VisitMark newMark = generateDefaultVisitMark(user, ResourceType.KANBAN, kanban.getId(), kanban.getName());
            newMark.setProjectId(kanban.getProject().getId());
            newMark.setProjectName(kanban.getProject().getName());
            user.getVisitMarks().add(visitMarkRepository.save(newMark));
            userRepository.save(user);
        }
    }

    public void updateVisitMarks(User user, Project project){
        Optional<VisitMark> visitMark = user.getVisitMarks().stream()
                .filter(v -> v.getResourceType() == ResourceType.PROJECT
                        && v.getResourceId() == project.getId()).findAny();
        if(visitMark.isPresent()) {
            transportMark(user, visitMark.get());
        } else {
            VisitMark newMark = generateDefaultVisitMark(user, ResourceType.PROJECT, project.getId(), project.getName());
            newMark.setDescription(project.getDescription());
            user.getVisitMarks().add(visitMarkRepository.save(newMark));
            userRepository.save(user);
        }
    }

    public void redactVisitMark(Project project) {
        project.getConnectors().stream()
                .map(UserWithProjectConnector::getUser)
                .flatMap(user -> user.getVisitMarks().stream())
                .filter(visitMark -> visitMark.getResourceType() == ResourceType.PROJECT)
                .filter(visitMark -> visitMark.getResourceId() == project.getId())
                .forEach(visitMark -> {
                    visitMark.setDescription(project.getDescription());
                    visitMark.setResourceName(project.getName());
                    visitMarkRepository.save(visitMark);
                });
    }

    public void redactVisitMark(Kanban kanban){
        kanban.getProject().getConnectors().stream()
                .map(UserWithProjectConnector::getUser)
                .flatMap(user -> user.getVisitMarks().stream())
                .filter(visitMark -> visitMark.getResourceType() == ResourceType.KANBAN)
                .filter(visitMark -> visitMark.getResourceId() == kanban.getId())
                .forEach(visitMark -> {
                    visitMark.setResourceName(kanban.getName());
                    visitMarkRepository.save(visitMark);
                });
    }

    public void redactVisitMark(Page page) {
        page.getProject().getConnectors().stream()
                .map(UserWithProjectConnector::getUser)
                .flatMap(user -> user.getVisitMarks().stream())
                .filter(visitMark -> visitMark.getResourceType() == ResourceType.DOCUMENT)
                .filter(visitMark -> visitMark.getResourceId() == page.getId())
                .forEach(visitMark -> {
                    visitMark.setResourceName(page.getName());
                    visitMarkRepository.save(visitMark);
                });
    }

    public void deleteVisitMark(Project project, long id, ResourceType resourceType) {
        project.getConnectors().parallelStream()
                .map(UserWithProjectConnector::getUser)
                .forEach(u -> u.getVisitMarks().stream()
                        .filter(visitMark -> visitMark.getResourceType() == resourceType)
                        .filter(visitMark -> visitMark.getResourceId() == id)
                        .findAny()
                        .ifPresent(visitMark -> {
                            u.getVisitMarks().remove(visitMark);
                            visitMarkRepository.delete(visitMark);
                        }));
    }

    private VisitMark generateDefaultVisitMark(User user, ResourceType resourceType, long id, String name) {
        user.getVisitMarks().forEach(vm -> vm.setSerialNumber(vm.getSerialNumber() + 1));
        user.getVisitMarks().removeIf(vm -> vm.getSerialNumber() > 120);
        VisitMark newMark = new VisitMark();
        newMark.setSerialNumber(0);
        newMark.setResourceId(id);
        newMark.setResourceName(name);
        newMark.setResourceType(resourceType);
        return newMark;
    }

    private void transportMark(User user, VisitMark visitMark) {
        user.getVisitMarks().stream()
                .filter(vm -> vm.getSerialNumber() < visitMark.getSerialNumber())
                .forEach(vm -> {
                    vm.setSerialNumber(vm.getSerialNumber() + 1);
                    visitMarkRepository.save(vm);
                });
        visitMark.setSerialNumber(0);
        visitMarkRepository.save(visitMark);
    }
}
