package com.epam.indigoeln.core.service.project;

import com.epam.indigoeln.core.model.Project;
import java.util.Collection;

public interface ProjectService {

    Project saveProject(Project project);
    Collection<Project> getAllProjects();
    Project getProjectByName(String name);
    void deleteProjectByName(String name);
}
