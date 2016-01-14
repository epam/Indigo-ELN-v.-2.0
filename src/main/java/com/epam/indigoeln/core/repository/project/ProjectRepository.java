package com.epam.indigoeln.core.repository.project;

import com.epam.indigoeln.core.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public interface ProjectRepository extends MongoRepository<Project, String> {

    Project findByName(String name);

}
