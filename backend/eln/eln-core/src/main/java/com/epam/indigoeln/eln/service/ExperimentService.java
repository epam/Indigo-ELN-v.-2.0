package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.util.ModelUtil;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class ExperimentService {

    static final byte[] EMPTY_PICTURE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\"/>".getBytes(StandardCharsets.UTF_8);

    @Inject
    NotebookRepository notebookRepository;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ACLService aclService;
    @Inject
    ExperimentMapper experimentMapper;
    @Inject
    UserService userService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    ExperimentModelService experimentModelService;

    public ExperimentDetailsDTO createExperiment(UUID notebookId, ExperimentRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, AccessOperation.CREATE_EXPERIMENT);
        ExperimentEntity experiment = experimentMapper.requestToExperiment(request, ExperimentStatus.OPEN);
        experiment.setName(generateExperimentName(notebook));
        experiment.setTherapeuticArea(dictionaryService.lookup(Dictionary.THERAPEUTIC_AREA, request.getTherapeuticArea()));
        experiment.setProjectCode(dictionaryService.lookup(Dictionary.PROJECT_CODE, request.getProjectCode()));
        notebook.getProject().getExperiments().add(experiment);
        notebook.getExperiments().add(experiment);
        experiment.setProject(notebook.getProject());
        experiment.setNotebook(notebook);
        experiment.setModel(experimentModelService.serializeModel(experimentModelService.createNewModel()));
        ModelUtil.updateDates(experiment, userService.getCurrentUser());
        aclService.initExperimentACL(experiment);
        experimentRepository.persist(experiment);
        experimentRepository.flushAndClear();
        return getExperiment(experiment.getId());
    }

    public Page<ExperimentDTO> getExperiments(@Nullable UUID projectId, @Nullable UUID notebookId, Paging paging) {
        var list = experimentRepository.findAll(projectId, notebookId, paging);
        return Page.of(paging, list.total(), list.list());
    }

    public List<ExperimentDTO> getMarkedExperiments() {
        return experimentRepository.findMarked();
    }

    public ExperimentDetailsDTO getExperiment(UUID experimentId) {
        return experimentRepository.load(experimentId);
    }

    public ExperimentDetailsDTO editExperiment(UUID experimentId, ExperimentEditRequest request) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, AccessOperation.EDIT);
        editProperty(request.getTherapeuticArea(), v -> {
            experiment.setTherapeuticArea(dictionaryService.lookup(Dictionary.THERAPEUTIC_AREA, v));
        });
        editProperty(request.getProjectCode(), v -> {
            experiment.setProjectCode(dictionaryService.lookup(Dictionary.PROJECT_CODE, v));
        });
        ModelUtil.updateDates(experiment, userService.getCurrentUser());
        experimentRepository.flushAndClear();
        return getExperiment(experimentId);
    }

    public Boolean markExperiment(UUID experimentId, boolean isMarked) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        experimentRepository.markExperiment(experimentId, userService.getCurrentUser(), isMarked);
        return isMarked;
    }

    public List<ACLEntryDTO> updateExperimentAccess(UUID experimentId, List<AccessForm> form) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, AccessOperation.MANAGE_PERMISSIONS);
        for (AccessForm item : form) {
            UserEntity user = userService.getUserEntity(item.getUserID());
            aclService.updateExperimentACL(experiment.getNotebook().getProject(), experiment.getNotebook(), experiment, user, item.getLevel());
        }
        return experimentMapper.convertACLMap(experiment.getAclEntities());
    }

    public ExperimentModel getModel(UUID experimentId) {
        return experimentModelService.deserializeModel(experimentRepository.get(experimentId).getModel());
    }

    public ExperimentModel mutateModel(UUID experimentId, ExperimentModel model, Mutation mutation) {
        try {
            log.debug("Mutating model for experiment {} with mutation {}", experimentId, mutation);
            ExperimentEntity experiment = experimentRepository.get(experimentId);
            model = experimentModelService.applyMutation(experiment, model, mutation);
            String modelStr = experimentModelService.serializeModel(model);
            experiment.setModel(modelStr);
            return model;
        } catch (Throwable e) {
            log.error("Failed to mutate model for experiment {}: {}", experimentId, e.getMessage(), e);
            throw new RuntimeException("Failed to mutate model", e);
        }
    }

    public byte[] getExperimentPicture(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, AccessOperation.VIEW);
        return experiment.getPicture() != null ? experiment.getPicture() : EMPTY_PICTURE;
    }

    private String generateExperimentName(NotebookEntity notebook) {
        String last = experimentRepository.getLastExperimentName(notebook);
        int lastNumber = last == null ? 0 : Integer.parseInt(last.substring(last.lastIndexOf('-') + 1));
        return "%s-%04d".formatted(notebook.getName(), lastNumber + 1);
    }
}
