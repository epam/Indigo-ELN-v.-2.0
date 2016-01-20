package com.epam.indigoeln.core.repository.experiment;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.epam.indigoeln.web.rest.dto.ExperimentShortDTO;

import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;

public interface ExperimentRepository extends MongoRepository<Experiment, String> {

    Collection<Experiment> findByAuthor(User author);

    Collection<Experiment> findByTemplateId(String templateId);

    Long countByTemplateId(String templateId);

    @Query(value="{ 'project' : ?0 }", fields="{ 'id' : 1, 'title' : 1, 'experimentNumber' : 1, 'templateId' : 1, 'project' : 1}")
    Collection<ExperimentShortDTO> findExperimentsByProject(String project);
}
