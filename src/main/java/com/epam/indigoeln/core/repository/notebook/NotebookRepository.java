package com.epam.indigoeln.core.repository.notebook;

import com.epam.indigoeln.core.model.Notebook;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NotebookRepository extends MongoRepository<Notebook, String> {

    Optional<Notebook> findOneByName(String name);
}
