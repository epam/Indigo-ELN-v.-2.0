package com.epam.indigoeln.core.repository.template;

import com.epam.indigoeln.core.model.Template;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TemplateRepository extends MongoRepository<Template, String> {

    Optional<Template> findOneByName(String name);

}