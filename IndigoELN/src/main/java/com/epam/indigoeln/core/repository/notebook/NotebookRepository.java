package com.epam.indigoeln.core.repository.notebook;

import com.epam.indigoeln.core.model.Notebook;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface NotebookRepository extends MongoRepository<Notebook, String> {

    @Query("{'experiments.$id': ?0}")
    Notebook findByExperimentId(String experimentId);

    Notebook findByName(String name);
}