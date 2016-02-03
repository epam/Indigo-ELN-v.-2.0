package com.epam.indigoeln.core.repository.component;

import com.epam.indigoeln.core.model.Component;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComponentRepository extends MongoRepository<Component, String> {
}
