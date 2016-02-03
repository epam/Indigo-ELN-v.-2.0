package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import com.epam.indigoeln.web.rest.dto.ExperimentTablesDTO;
import com.epam.indigoeln.web.rest.util.PermissionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExperimentService {

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private ComponentRepository componentRepository;

    public Collection<Experiment> getAllExperiments() {
        return experimentRepository.findAll();
    }

    public Collection<Experiment> getAllExperiments(String notebookId, User user) {
        Notebook notebook = notebookRepository.findOne(notebookId);
        if(notebook == null) {
            throw EntityNotFoundException.createWithNotebookId(notebookId);
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in notebook's access list,
        // or must have ADMIN authority)
        if (PermissionUtil.isContentEditor(user)) {
            return notebook.getExperiments();
        } else if (PermissionUtil.hasPermissions(user, notebook.getAccessList(),
                UserPermission.READ_SUB_ENTITY)) {
            return getExperimentsWithAccess(notebook.getExperiments(), user.getId());
        }
        throw new AccessDeniedException("Current user doesn't have permissions to read " +
                "experiments of notebook with id = " + notebook.getId());
    }

    public Experiment getExperiment(String id, User user) {
        Experiment experiment = experimentRepository.findOne(id);
        if (experiment == null) {
            throw EntityNotFoundException.createWithExperimentId(id);
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in notebook's access list and
        // "Read Entity" in experiment's access list, or must have ADMIN authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(id);
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(id);
            }

            if (!PermissionUtil.hasPermissions(user,
                    notebook.getAccessList(), UserPermission.READ_SUB_ENTITY,
                    experiment.getAccessList(), UserPermission.READ_ENTITY)) {
                throw new AccessDeniedException("Current user doesn't have permissions " +
                        "to read experiment with id = " + experiment.getId());
            }
        }
        return experiment;
    }

    public Collection<Experiment> getExperimentsByAuthor(User user) {
        return experimentRepository.findByAuthor(user);
    }

    public Experiment createExperiment(Experiment experiment, String notebookId, User user) {
        Notebook notebook = notebookRepository.findOne(notebookId);
        if (notebook == null) {
            throw EntityNotFoundException.createWithNotebookId(notebookId);
        }

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list,
        // or must have ADMIN authority)
        if (!PermissionUtil.hasPermissions(user, notebook.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw new AccessDeniedException(
                    "Current user doesn't have permissions to create experiment");
        }

        // Adding of OWNER's permissions for specified User to experiment
        PermissionUtil.addOwnerToAccessList(experiment.getAccessList(), user.getId());

        experiment.setComponents(updateComponents(null, experiment.getComponents()));
        experiment = experimentRepository.save(experiment);

        notebook.getExperiments().add(experiment);
        notebookRepository.save(notebook);
        return experiment;
    }

    public Experiment updateExperiment(Experiment experimentForSave, User user) {
        Experiment experimentFromDB = experimentRepository.findOne(experimentForSave.getId());
        if (experimentFromDB == null) {
            throw EntityNotFoundException.createWithExperimentId(experimentForSave.getId());
        }

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list and
        // "Update Entity" in experiment's access list, or must have ADMIN authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(experimentForSave.getId());
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(experimentForSave.getId());
            }

            if (!PermissionUtil.hasPermissions(user,
                    notebook.getAccessList(), UserPermission.CREATE_SUB_ENTITY,
                    experimentFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                throw new AccessDeniedException(
                        "Current user doesn't have permissions to update experiment with id = " + experimentForSave.getId());
            }
        }

        experimentFromDB.setExperimentNumber(experimentForSave.getExperimentNumber());
        experimentFromDB.setTitle(experimentForSave.getTitle());
        experimentFromDB.setProject(experimentForSave.getProject());
        experimentFromDB.setTemplateId(experimentForSave.getTemplateId());
        experimentFromDB.setAccessList(experimentForSave.getAccessList());
        experimentFromDB.setAuthor(experimentForSave.getAuthor());
        experimentFromDB.setCoAuthors(experimentForSave.getCoAuthors());
        experimentFromDB.setComments(experimentForSave.getComments());
        experimentFromDB.setFileIds(experimentForSave.getFileIds());
        experimentFromDB.setStatus(experimentForSave.getStatus());
        experimentFromDB.setWitness(experimentForSave.getWitness());

        experimentFromDB.setComponents(updateComponents(experimentFromDB.getComponents(), experimentForSave.getComponents()));

        return experimentRepository.save(experimentFromDB);
    }

    private List<Component> updateComponents(List<Component> oldComponents, List<Component> newComponents) {

        List<Component> componentsFromDb = oldComponents != null ? oldComponents : Collections.emptyList();
        List<String> componentIdsForRemove = componentsFromDb.stream().map(Component::getId).collect(Collectors.toList());

        List<Component> componentsForSave = new ArrayList<>();
        for(Component component : newComponents) {
            if(component.getId() != null) {
                Optional<Component> existing = componentsFromDb.stream().filter(c -> c.getId().equals(component.getId())).findFirst();
                if(existing.isPresent()) {
                    Component componentForSave = existing.get();
                    componentForSave.setBingoDbId(component.getBingoDbId());
                    componentForSave.setComponentTemplateId(component.getComponentTemplateId());
                    componentForSave.setContent(component.getContent());
                    componentIdsForRemove.remove(componentForSave.getId());
                    componentsForSave.add(componentForSave);
                } else {
                    throw new ValidationException("Cannot find component with id=" + component.getId());
                }
            } else {
                componentsForSave.add(component);
            }
        }

        componentRepository.delete(
                componentsFromDb.stream().filter(c -> componentIdsForRemove.contains(c.getId())).collect(Collectors.toList()));

        return componentRepository.save(componentsForSave);
    }


    public void deleteExperiment(String id) {
        Experiment experiment = experimentRepository.findOne(id);
        if (experiment == null) {
            throw EntityNotFoundException.createWithExperimentId(id);
        }

        Notebook notebook = notebookRepository.findByExperimentId(experiment.getId());
        if (notebook == null) {
            throw EntityNotFoundException.createWithNotebookChildId(experiment.getId());
        }

        notebook.getExperiments().remove(experiment);
        notebookRepository.save(notebook);

        experimentRepository.delete(experiment);
    }

    public boolean hasExperiments(Notebook notebook, User user) {
        if (PermissionUtil.isContentEditor(user)) {
            return !notebook.getExperiments().isEmpty();
        } else {
            UserPermission userPermission = PermissionUtil.findPermissionsByUserId(
                    notebook.getAccessList(), user.getId());
            // Checking userPermission for "Read Sub-Entity" possibility,
            // and that notebook has experiments with UserPermission for specified User
            return userPermission != null && userPermission.canReadSubEntity() &&
                    hasExperimentsWithAccess(notebook.getExperiments(), user.getId());
        }
    }

    private static boolean hasExperimentsWithAccess(List<Experiment> experiments, String userId) {
        for (Experiment experiment : experiments) {
            if (PermissionUtil.findPermissionsByUserId(experiment.getAccessList(), userId) != null) {
                // Because we have one at least Experiment with UserPermission for Read Entity
                return true;
            }
        }
        return false;
    }

    private static List<Experiment> getExperimentsWithAccess(List<Experiment> experiments, String userId) {
        return experiments.stream().filter(experiment -> PermissionUtil.findPermissionsByUserId(
                experiment.getAccessList(), userId) != null).collect(Collectors.toList());
    }

    public ExperimentTablesDTO getExperimentTables() {
        ExperimentTablesDTO dto = new ExperimentTablesDTO();
        User author = new User();
        author.setLogin("UserAuthor");
        User coAuthor = new User();
        coAuthor.setLogin("UserCoAuthor");
        User witness = new User();
        witness.setLogin("UserWitness");

        List<User> witnessList = new ArrayList<>();
        List<User> coauthorsList = new ArrayList<>();

        witnessList.add(witness);
        coauthorsList.add(coAuthor);
        coauthorsList.add(witness);

        Experiment experiment1 = new Experiment();
        experiment1.setId("AAA111");
        experiment1.setTitle("Elixir of immortality");
        experiment1.setAuthor(author);
        experiment1.setCoAuthors(coauthorsList);
        experiment1.setComments("Awesome Experiment 1");
        experiment1.setCreationDate(LocalDate.now());
        experiment1.setLastEditDate(LocalDate.now());
        experiment1.setLastModifiedBy(author);
        experiment1.setProject("Cool Project 1");
        experiment1.setStatus("Completed");
        experiment1.setWitness(witnessList);

        Experiment experiment2 = new Experiment();
        experiment2.setId("AAA112");
        experiment2.setTitle("Potion of happiness");
        experiment2.setAuthor(author);
        experiment2.setCoAuthors(coauthorsList);
        experiment2.setComments("Awesome Experiment 2");
        experiment2.setCreationDate(LocalDate.now());
        experiment2.setLastEditDate(LocalDate.now());
        experiment2.setLastModifiedBy(author);
        experiment2.setProject("Cool Project 2");
        experiment2.setStatus("Submitted");
        experiment2.setWitness(witnessList);

        Experiment experiment3 = new Experiment();
        experiment3.setId("AAA113");
        experiment3.setTitle("Potion of joy");
        experiment3.setAuthor(author);
        experiment3.setCoAuthors(coauthorsList);
        experiment3.setComments("Awesome Experiment 3");
        experiment3.setCreationDate(LocalDate.now());
        experiment3.setLastEditDate(LocalDate.now());
        experiment3.setLastModifiedBy(author);
        experiment3.setProject("Cool Project 3");
        experiment3.setStatus("Archived");
        experiment3.setWitness(witnessList);

        Experiment experiment4 = new Experiment();
        experiment4.setId("AAA114");
        experiment4.setTitle("Elixir of time");
        experiment4.setAuthor(author);
        experiment4.setCoAuthors(coauthorsList);
        experiment4.setComments("Awesome Experiment 4");
        experiment4.setCreationDate(LocalDate.now());
        experiment4.setLastEditDate(LocalDate.now());
        experiment4.setLastModifiedBy(author);
        experiment4.setProject("Cool Project 4");
        experiment4.setStatus("Signing");
        experiment4.setWitness(witnessList);

        Experiment[] set1 = {experiment1, experiment2, experiment3, experiment4};
        Experiment[] set2 = {experiment1, experiment2, experiment3};
        Experiment[] set3 = {experiment3, experiment4};

        dto.setOpenAndCompletedExp(Arrays.asList(set1));
        dto.setWaitingSignatureExp(Arrays.asList(set2));
        dto.setSubmittedAndSigningExp(Arrays.asList(set3));
        return dto;
    }
}