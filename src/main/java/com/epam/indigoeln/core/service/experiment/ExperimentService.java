package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.web.dto.ExperimentTablesDTO;

import java.util.Collection;
import java.util.List;

public interface ExperimentService {

    Experiment save(Experiment experiment);

    List<Experiment> findAll();

    Experiment findOne(String id);

    Collection<Experiment> findByAuthor(User author);

    void delete(String id);

    ExperimentTablesDTO getExperimentTables();

}
