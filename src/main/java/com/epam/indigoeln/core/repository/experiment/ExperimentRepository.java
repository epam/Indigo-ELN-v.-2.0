package com.epam.indigoeln.core.repository.experiment;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface ExperimentRepository extends MongoRepository<Experiment, String> {

    Collection<Experiment> findByAuthor(User author);

    Collection<Experiment> findByAuthorOrSubmittedBy(User author, User submittedBy);

    @Query("{'fileIds': ?0}")
    Experiment findByFileId(String fileId);

    List<Experiment> findByStatusIn(List<ExperimentStatus> statuses);

    Stream<Experiment> findByAuthorAndStatusAndCreationDateAfter(User user, ExperimentStatus status, ZonedDateTime creationTime);

    Stream<Experiment> findByDocumentIdIn(Collection<String> documentsIds);

}