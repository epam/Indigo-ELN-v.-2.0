package com.epam.indigoeln.core.repository.project;

import com.epam.indigoeln.core.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

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
        return mongoTemplate.findOne(query(where("projectName").is(name)), Project.class);
    }

    public void deleteProjectByName(String name) {
        mongoTemplate.remove(query(where("projectName").is(name)), Project.class);
    }
}
