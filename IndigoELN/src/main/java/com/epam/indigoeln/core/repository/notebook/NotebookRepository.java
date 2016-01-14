package com.epam.indigoeln.core.repository.notebook;

import com.epam.indigoeln.core.model.Notebook;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotebookRepository extends MongoRepository<Notebook, String> {
}
