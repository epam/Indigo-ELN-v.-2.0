package com.epam.indigoeln.core.repository.template;

import com.epam.indigoeln.core.model.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TemplateRepository extends MongoRepository<Template, String> {

    Optional<Template> findOneByName(String name);

    Page<Template> findByNameLikeIgnoreCase(String name, Pageable pageable);
}