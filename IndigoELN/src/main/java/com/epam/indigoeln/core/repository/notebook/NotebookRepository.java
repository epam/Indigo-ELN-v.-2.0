package com.epam.indigoeln.core.repository.notebook;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface NotebookRepository extends MongoRepository<Notebook, String> {

    @Query("{'experiments': {'$ref': '" + Experiment.COLLECTION_NAME + "', '$id': ?0}}")
    Notebook findByExperimentId(String experimentId);

    Optional<Notebook> findByName(String name);

}