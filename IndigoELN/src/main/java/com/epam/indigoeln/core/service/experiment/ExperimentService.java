package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.sequenceid.SequenceIdRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.component.name.GenerateNameService;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.*;
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

    public List<TreeNodeDTO> getAllExperimentTreeNodes(Long notebookId) {
        return getAllExperimentTreeNodes(notebookId, null);
    }

    public List<TreeNodeDTO> getAllExperimentTreeNodes(Long notebookId, User user) {
        Collection<Experiment> experiments = getAllExperiments(notebookId, user);
        return experiments.stream().
                map(experiment -> new TreeNodeDTO(new ExperimentDTO(experiment))).
                collect(Collectors.toList());
    }

    /**
     * If user is null, then retrieve experiments without checking for UserPermissions
     * Otherwise, use checking for UserPermissions
     */
    private Collection<Experiment> getAllExperiments(Long notebookId, User user) {
        Notebook notebook = notebookRepository.findOneBySequenceId(notebookId).
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId.toString()));

        if (user == null) {
            return notebook.getExperiments();
        }

        // Check of EntityAccess (User must have "Read Sub-Entity" permission in notebook's access list)
        if (!PermissionUtil.hasPermissions(user.getId(), notebook.getAccessList(),
                UserPermission.READ_SUB_ENTITY)) {
            throw OperationDeniedException.createNotebookSubEntitiesReadOperation(notebook.getId());
        }

        return getExperimentsWithAccess(notebook.getExperiments(), user.getId());
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
                throw OperationDeniedException.createExperimentReadOperation(experiment.getId());
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
            throw OperationDeniedException.createNotebookSubEntityCreateOperation(notebook.getId());
        }

        Experiment experiment = dtoMapper.convertFromDTO(experimentDTO);
        // reset experiment's id
        experiment.setId(null);
        //generate name
        experiment.setName(generateNameService.generateExperimentName(notebookSequenceId));
        if (experimentDTO.getTemplate() != null) {
            Template template = new Template();
            template.setTemplateContent(experimentDTO.getTemplate().getTemplateContent());
            experiment.setTemplate(template);
        }
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
                throw OperationDeniedException.createExperimentUpdateOperation(experimentFromDB.getId());
            }
        }

        Experiment experimentForSave = dtoMapper.convertFromDTO(experimentDTO);
        if (experimentDTO.getTemplate() != null) {
            Template template = new Template();
            template.setTemplateContent(experimentDTO.getTemplate().getTemplateContent());
            experimentForSave.setTemplate(template);
        }

        // check of user permissions's correctness in access control list
        PermissionUtil.checkCorrectnessOfAccessList(userRepository, experimentForSave.getAccessList());

        experimentFromDB.setTemplate(experimentForSave.getTemplate());
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
        for (Component component : newComponents) {
            if (component.getId() != null) {
                Optional<Component> existing = componentsFromDb.stream().filter(c -> c.getId().equals(component.getId())).findFirst();
                if (existing.isPresent()) {
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
        return experiments == null ? new ArrayList<>() :
                experiments.stream().filter(experiment -> PermissionUtil.findPermissionsByUserId(
                        experiment.getAccessList(), userId) != null).collect(Collectors.toList());
    }
}