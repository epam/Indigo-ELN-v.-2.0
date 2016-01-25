package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;

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
}