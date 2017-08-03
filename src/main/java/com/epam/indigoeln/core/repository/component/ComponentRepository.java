package com.epam.indigoeln.core.repository.component;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.epam.indigoeln.core.model.Component;

public interface ComponentRepository extends MongoRepository<Component, String> {

    @Query("{ 'name' :'productBatchSummary', 'content.batches' : {$elemMatch : {'registrationJobId' : ?0}}}")
    List<Component> findBatchSummariesByRegistrationJobId(String jobId);

    @Query("{ 'name' :'productBatchSummary', 'content.batches' : {$elemMatch : {'fullNbkBatch' : {$in : ?0}}}}")
    List<Component> findBatchSummariesByFullBatchNumbers(List<String> fullNbkBatchNumbers);

    @Query("{ 'name' :'preferredCompoundSummary', 'content.compounds' : {$elemMatch : {'fullNbkBatch' : {$in : ?0}}}}")
    List<Component> findPreferredCompoundSummariesByFullBatchNumbers(List<String> fullNbkBatchNumbers);

    @Query(value="{'id' : { $in : ?0}}", delete = true)
    void deleteAllById(List<String> componentIds);

}
