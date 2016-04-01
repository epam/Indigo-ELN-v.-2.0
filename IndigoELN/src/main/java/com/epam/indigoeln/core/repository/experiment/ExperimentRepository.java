package com.epam.indigoeln.core.repository.experiment;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ExperimentRepository extends MongoRepository<Experiment, String> {

    Collection<Experiment> findByAuthor(User author);

    @Query("{'fileIds': ?0}")
    Experiment findByFileId(String fileId);

    @Query(value="{'status' : { $in : ?0}}")
    List<Experiment> findByStatuses(List<ExperimentStatus> statuses);
}