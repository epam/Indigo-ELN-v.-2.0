package com.epam.indigoeln.core.repository.template;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.epam.indigoeln.core.model.Template;


public interface TemplateRepository extends MongoRepository<Template, String> {

    Template findOneByName(String name);
}
