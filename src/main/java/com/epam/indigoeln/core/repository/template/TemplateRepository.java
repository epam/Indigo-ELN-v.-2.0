package com.epam.indigoeln.core.repository.template;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.epam.indigoeln.core.model.ExperimentTemplate;


public interface TemplateRepository extends MongoRepository<ExperimentTemplate, String> {

    ExperimentTemplate findOneByName(String name);
}
