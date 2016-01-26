package com.epam.indigoeln.core.repository.template.experiment;


import org.springframework.data.mongodb.repository.MongoRepository;
import com.epam.indigoeln.core.model.ExperimentTemplate;

public interface ExperimentTemplateRepository extends MongoRepository<ExperimentTemplate, String> {

    ExperimentTemplate findOneByName(String name);
}
