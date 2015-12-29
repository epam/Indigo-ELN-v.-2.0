package com.epam.indigoeln.core.service.project;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public Project saveProject(Project project) {
        return projectRepository.saveProject(project);
    }

    @Override
    public Collection<Project> getAllProjects() {
        return projectRepository.getAllProjects();
    }

    @Override
    public Project getProjectByName(String name) {
        return projectRepository.getProjectByName(name);
    }

    @Override
    public void deleteProjectByName(String name) {
        projectRepository.deleteProjectByName(name);
    }
}
