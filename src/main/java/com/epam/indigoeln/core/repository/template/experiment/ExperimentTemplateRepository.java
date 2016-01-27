package com.epam.indigoeln.core.repository.template.experiment;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.epam.indigoeln.core.model.ExperimentTemplate;


public interface ExperimentTemplateRepository extends MongoRepository<ExperimentTemplate, String> {

    ExperimentTemplate findOneByName(String name);

    /**
     * Get count of templates, that has componentTemplate with component template id
     * @param componentId id of component template
     * @return count of templates
     */
    @Query(value = "{ 'components': { $elemMatch: { $ref: 'component_template', $id: ?0 } } }", count = true)
    Long countByComponentId(String componentId);
}
