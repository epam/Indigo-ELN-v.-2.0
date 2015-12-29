package com.epam.indigoeln.core.repository.project;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class ProjectRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Project saveProject(Project project) {
        mongoTemplate.save(project);
        return project;
    }

    public Collection<Project> getAllProjects() {
        return mongoTemplate.findAll(Project.class);
    }

    public Project getProjectByName(String name) {
        Query searchQuery = new Query(Criteria.where("projectName").is(name));
        return mongoTemplate.findOne(searchQuery, Project.class);
    }

    public void deleteProjectByName(String name) {
        Query searchQuery = new Query(Criteria.where("projectName").is(name));
        mongoTemplate.remove(searchQuery, Project.class);
    }
}
