package com.epam.indigoeln.core.repository.template;

import com.epam.indigoeln.core.model.Template;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TemplateRepository extends MongoRepository<Template, String> {

    Template findOneByName(String name);
}
