package com.epam.indigoeln.core.repository.component;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.epam.indigoeln.core.model.Component;

public interface ComponentRepository extends MongoRepository<Component, String> {

    @Query("{ 'name' :'productBatchSummary', 'content.batches' : {$elemMatch : {'structure.structureId' : {$in : ?0}}}}")
    List<Component> findBatchSummariesByBingoDbIds(List<Integer> bingoIds);

    @Query("{ 'name' :'productBatchSummary', 'content.batches' : {$elemMatch : {'registrationJobId' : ?0}}}")
    List<Component> findBatchSummariesByRegistrationJobId(Long jobId);


    @Query("{ 'name' :'productBatchSummary', 'content.batches' : {$elemMatch : {'fullNbkBatch' : ?0}}}")
    Optional<Component> findBatchSummaryByFullBatchNumber(String fullNbkBatchNumber);

    @Query("{ 'name' :'productBatchSummary', 'content.batches' : {$elemMatch : {'fullNbkBatch' : {$in : ?0}}}}")
    List<Component> findBatchSummariesByFullBatchNumbers(List<String> fullNbkBatchNumbers);

    @Query(value="{'id' : { $in : ?0}}", delete = true)
    void deleteAllById(List<String> componentIds);

}
