package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.sequenceid.SequenceIdRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import com.epam.indigoeln.core.service.component.number.GenerateNameService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;

import java.util.ArrayList;
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

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SequenceIdRepository sequenceIdRepository;

    @Autowired
    private GenerateNameService generateNameService;

    @Autowired
    CustomDtoMapper dtoMapper;


    public Collection<Experiment> getAllExperiments() {
        return experimentRepository.findAll();
    }

    public Collection<ExperimentDTO> getAllExperiments(Long notebookId, User user) {
        Notebook notebook = notebookRepository.findOneBySequenceId(notebookId).
                orElseThrow(() ->  EntityNotFoundException.createWithNotebookId(notebookId.toString()));

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in notebook's access list)
        if (!PermissionUtil.hasPermissions(user.getId(), notebook.getAccessList(),
                UserPermission.READ_SUB_ENTITY)) {
            throw new AccessDeniedException("The current user doesn't have permissions to read " +
                    "experiments of notebook with id = " + notebook.getId());
        }

        return getExperimentsWithAccess(notebook.getExperiments(), user.getId()).
                    stream().map(ExperimentDTO::new).collect(Collectors.toList());
    }

    public ExperimentDTO getExperiment(Long sequenceId, User user) {
        Experiment experiment = experimentRepository.findOneBySequenceId(sequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(sequenceId.toString()));

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in notebook's access list and
        // "Read Entity" in experiment's access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(experiment.getId());
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(experiment.getSequenceId().toString());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    notebook.getAccessList(), UserPermission.READ_SUB_ENTITY,
                    experiment.getAccessList(), UserPermission.READ_ENTITY)) {
                throw new AccessDeniedException("The current user doesn't have permissions " +
                        "to read experiment with id = " + experiment.getId());
            }
        }
        return new ExperimentDTO(experiment);
    }

    public Collection<ExperimentDTO> getExperimentsByAuthor(User user) {
        return experimentRepository.findByAuthor(user).stream().map(ExperimentDTO::new).collect(Collectors.toList());
    }

    public ExperimentDTO createExperiment(ExperimentDTO experimentDTO, Long notebookSequenceId, User user) {
        Notebook notebook = notebookRepository.findOneBySequenceId(notebookSequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookSequenceId.toString()));

        // check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list,
        // or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, notebook.getAccessList(),
                UserPermission.CREATE_SUB_ENTITY)) {
            throw new AccessDeniedException(
                    "The current user doesn't have permissions to create experiment");
        }

        Experiment experiment = dtoMapper.convertFromDTO(experimentDTO);
        // reset experiment's id
        experiment.setId(null);
        //generate name
        experiment.setName(generateNameService.generateExperimentName(notebookSequenceId));
        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, experiment.getAccessList());
        // add OWNER's permissions for specified User to experiment
        PermissionUtil.addOwnerToAccessList(experiment.getAccessList(), user);

        experiment.setComponents(updateComponents(null, experiment.getComponents()));
        //increment sequence Id
        experiment.setSequenceId(sequenceIdRepository.getNextExperimentId());
        experiment = experimentRepository.save(experiment);

        notebook.getExperiments().add(experiment);
        notebookRepository.save(notebook);
        return new ExperimentDTO(experiment);
    }

    public ExperimentDTO updateExperiment(ExperimentDTO experimentDTO, User user) {
        Experiment experimentFromDB = experimentRepository.findOneBySequenceId(experimentDTO.getSequenceId()).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentDTO.getSequenceId().toString()));

        // Check of EntityAccess (User must have "Create Sub-Entity" permission in notebook's access list and
        // "Update Entity" in experiment's access list, or must have CONTENT_EDITOR authority)
        if (!PermissionUtil.isContentEditor(user)) {
            Notebook notebook = notebookRepository.findByExperimentId(experimentFromDB.getId());
            if (notebook == null) {
                throw EntityNotFoundException.createWithNotebookChildId(experimentFromDB.getId());
            }

            if (!PermissionUtil.hasPermissions(user.getId(),
                    notebook.getAccessList(), UserPermission.CREATE_SUB_ENTITY,
                    experimentFromDB.getAccessList(), UserPermission.UPDATE_ENTITY)) {
                throw new AccessDeniedException(
                        "The current user doesn't have permissions to update experiment with id = " + experimentDTO.getSequenceId());
            }
        }

        Experiment experimentForSave = dtoMapper.convertFromDTO(experimentDTO);

        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, experimentForSave.getAccessList());

        experimentFromDB.setTemplateId(experimentForSave.getTemplateId());
        experimentFromDB.setAccessList(experimentForSave.getAccessList());
        experimentFromDB.setCoAuthors(experimentForSave.getCoAuthors());
        experimentFromDB.setComments(experimentForSave.getComments());
        experimentFromDB.setStatus(experimentForSave.getStatus());
        experimentFromDB.setWitness(experimentForSave.getWitness());

        experimentFromDB.setComponents(updateComponents(experimentFromDB.getComponents(), experimentForSave.getComponents()));

        return new ExperimentDTO(experimentRepository.save(experimentFromDB));
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
                    componentForSave.setContent(component.getContent());
                    componentIdsForRemove.remove(componentForSave.getId());
                    componentForSave.setName(componentForSave.getName());
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


    public void deleteExperiment(Long sequenceId, Long notebookSequenceId) {
        Experiment experiment = experimentRepository.findOneBySequenceId(sequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(sequenceId.toString()));

        Notebook notebook = notebookRepository.findOneBySequenceId(notebookSequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookChildId(experiment.getId()));

        notebook.getExperiments().remove(experiment);
        notebookRepository.save(notebook);

        //delete experiment components
        Optional.ofNullable(experiment.getComponents()).ifPresent(components ->
            componentRepository.deleteAllById(components.stream().map(Component::getId).collect(Collectors.toList()))
        );

        fileRepository.delete(experiment.getFileIds());
        experimentRepository.delete(experiment);
    }

    private static List<Experiment> getExperimentsWithAccess(List<Experiment> experiments, String userId) {
        return  experiments == null ? new ArrayList<>() :
                experiments.stream().filter(experiment -> PermissionUtil.findPermissionsByUserId(
                    experiment.getAccessList(), userId) != null).collect(Collectors.toList());
    }
}