package com.epam.indigoeln.web.rest.util;


import com.epam.indigoeln.core.model.Batch;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.web.rest.dto.BatchDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.core.util.SequenceNumberGenerationUtil;

import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ConverterUtils {

    private ConverterUtils() {
    }

    public static Project convertFromDTO(ProjectDTO dto) {
        Project project = new Project();
        project.setId(dto.getId());
        project.setName(dto.getName());
        project.setAccessList(dto.getAccessList());

        return project;
    }

    public static Notebook convertFromDTO(NotebookDTO dto) {
        Notebook notebook = new Notebook();
        notebook.setId(dto.getId());
        notebook.setName(dto.getName());
        notebook.setAccessList(dto.getAccessList());

        return notebook;
    }

    public static Experiment convertFromDTO(ExperimentDTO dto) {
        Experiment experiment = new Experiment();
        experiment.setId(dto.getId());
        experiment.setTitle(dto.getTitle());
        experiment.setProject(dto.getProject());
        experiment.setExperimentNumber(dto.getExperimentNumber());
        experiment.setTemplateId(dto.getTemplateId());

        return experiment;
    }

    /**
     * Create new batch item from DTO
     * @param batchDTO batch DTO
     * @param experiment experiment
     * @return new Batch item converted from DTO
     */
    public static Batch convertFromDTO(BatchDTO batchDTO, Experiment experiment) {
        return mergeFromDTO(new Batch(), batchDTO, experiment);
    }

    /**
     * Merge batch DTO fields to batch item
     * @param batch batch item to be filled from Batch DTO
     * @param batchDTO DTO
     * @param experiment experiment
     * @return batch item filled from batch DTO
     */
    public static Batch mergeFromDTO(Batch batch, BatchDTO batchDTO, Experiment experiment) {
        batch.setId(Optional.ofNullable(batchDTO.getId()).orElse(ObjectId.get().toHexString()));
        batch.setStereoIsomerCode(batchDTO.getStereoIsomerCode());
        batch.setVirtualCompoundId(batchDTO.getVirtualCompoundId());
        batch.setComments(batchDTO.getComments());
        batch.setStructureComments(batchDTO.getStructureComments());
        List<String> allBatchNumbers = experiment.getBatches().stream().map(Batch::getBatchNumber).collect(Collectors.toList());

        batch.setBatchNumber(Optional.ofNullable(batchDTO.getBatchNumber())
                .orElse(SequenceNumberGenerationUtil.generateNextBatchNumber(allBatchNumbers)));
        return batch;
    }
}