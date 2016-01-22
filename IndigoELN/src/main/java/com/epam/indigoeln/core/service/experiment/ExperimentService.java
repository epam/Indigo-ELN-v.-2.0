package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import com.epam.indigoeln.core.util.SequenceNumberGenerationUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentTablesDTO;
import com.epam.indigoeln.web.rest.util.PermissionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperimentService {

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    public Collection<Experiment> getAllExperiments() {
        return experimentRepository.findAll();
    }

    public Collection<Experiment> getAllExperiments(@NotNull String notebookId, @NotNull User user) {
        Notebook notebook = notebookRepository.findOne(notebookId);
        if(notebook == null) {
            throw EntityNotFoundException.createWithNotebookId(notebookId);
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in notebook's access list,
        // or must have ADMIN authority)
        if (PermissionUtils.isAdmin(user)) {
            return notebook.getExperiments();
        } else if (PermissionUtils.hasPermissions(user, notebook.getAccessList(),
                UserPermission.READ_SUB_ENTITY)) {
            return getExperimentsWithAccess(notebook.getExperiments(), user.getId());
        }
        throw new AccessDeniedException("Current user doesn't have permissions to read " +
                "experiments of notebook with id = " + notebook.getId());
    }

    public Experiment getExperiment(@NotNull String id, @NotNull User user) {
        Experiment experiment = experimentRepository.findOne(id);
        if (experiment == null) {
            throw EntityNotFoundException.createWithExperimentId(id);
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in notebook's access list and
        // "Read Entity" in experiment's access list, or must have ADMIN authority)
        if (!PermissionUtils.isAdmin(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(id);
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(id);
            }

            if (!PermissionUtils.hasPermissions(user,
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

    public Experiment createExperiment(@NotNull Experiment experiment, @NotNull String notebookId, @NotNull User user) {
        Notebook notebook = notebookRepository.findOne(notebookId);
        if (notebook == null) {
            throw EntityNotFoundException.createWithNotebookId(notebookId);
        }

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list,
        // or must have ADMIN authority)
        if (!PermissionUtils.hasPermissions(user, notebook.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw new AccessDeniedException(
                    "Current user doesn't have permissions to create experiment");
        }

        // Adding of OWNER's permissions for specified User to experiment
        PermissionUtils.addOwnerToAccessList(experiment.getAccessList(), user.getId());
        validateAndSetExperimentNumber(experiment);
        experiment = experimentRepository.save(experiment);

        notebook.getExperiments().add(experiment);
        notebookRepository.save(notebook);
        return experiment;
    }

    public Experiment updateExperiment(@NotNull Experiment experiment, @NotNull User user) {
        Experiment experimentFromDB = experimentRepository.findOne(experiment.getId());
        if (experimentFromDB == null) {
            throw EntityNotFoundException.createWithExperimentId(experiment.getId());
        }

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list and
        // "Update Entity" in experiment's access list, or must have ADMIN authority)
        if (!PermissionUtils.isAdmin(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(experiment.getId());
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(experiment.getId());
            }

            if (!PermissionUtils.hasPermissions(user,
                    notebook.getAccessList(), UserPermission.CREATE_SUB_ENTITY,
                    experimentFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                throw new AccessDeniedException(
                        "Current user doesn't have permissions to update experiment with id = " + experiment.getId());
            }
        }
        validateAndSetExperimentNumber(experiment);
        return experimentRepository.save(experiment);
    }

    public void deleteExperiment(@NotNull String id) {
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

    public boolean hasExperiments(@NotNull Notebook notebook, @NotNull User user) {
        if (PermissionUtils.isAdmin(user)) {
            return !notebook.getExperiments().isEmpty();
        } else {
            UserPermission userPermission = PermissionUtils.findPermissionsByUserId(
                    notebook.getAccessList(), user.getId());
            // Checking userPermission for "Read Sub-Entity" possibility,
            // and that notebook has experiments with UserPermission for specified User
            return userPermission != null && userPermission.canReadSubEntity() &&
                    hasExperimentsWithAccess(notebook.getExperiments(), user.getId());
        }
    }

    private static boolean hasExperimentsWithAccess(List<Experiment> experiments, String userId) {
        for (Experiment experiment : experiments) {
            if (PermissionUtils.findPermissionsByUserId(experiment.getAccessList(), userId) != null) {
                // Because we have one at least Experiment with UserPermission for Read Entity
                return true;
            }
        }
        return false;
    }

    private static List<Experiment> getExperimentsWithAccess(List<Experiment> experiments, String userId) {
        return experiments.stream().filter(experiment -> PermissionUtils.findPermissionsByUserId(
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

    /**
     * Auto generate experiment number if not specified, otherwise
     * validate experiment number for uniqueness (experiment number is unique in Project)
     * @param experiment experiment
     * @return experiment enriched by experiment number
     * @throws ValidationException if experiment number isn't unique
     */
    private Experiment validateAndSetExperimentNumber(Experiment experiment) {
        Collection<ExperimentShort> projectExperiments = experimentRepository.findExperimentsByProject(experiment.getProject());
        Collection<String> projectExperimentNumbers = projectExperiments.stream().
                map(ExperimentShort::getExperimentNumber).collect(Collectors.toList());

        if(experiment.getExperimentNumber() == null) {
            experiment.setExperimentNumber(
                    SequenceNumberGenerationUtil.generateNextExperimentNumber(projectExperimentNumbers));
        } else if(projectExperimentNumbers.contains(experiment.getExperimentNumber())){
            throw new ValidationException(
                    String.format("The experiment number '%s' already exists in the system", experiment.getExperimentNumber())
            );
        }

        return experiment;
    }
}