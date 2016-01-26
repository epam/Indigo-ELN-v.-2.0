package com.epam.indigoeln.core.repository.template.component;

import com.epam.indigoeln.core.model.ComponentTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComponentTemplateRepository extends MongoRepository<ComponentTemplate, String> {
}
