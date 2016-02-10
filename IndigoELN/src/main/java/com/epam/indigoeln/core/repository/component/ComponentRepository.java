package com.epam.indigoeln.core.repository.component;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.epam.indigoeln.core.model.Component;

public interface ComponentRepository extends MongoRepository<Component, String> {

    @Query(value="{ 'content.component' : 'batchDetails', 'content.bingoDbId' : { $in : ?0 } }")
    List<Component> findBatchesByBingoDbIds(List<String> bingoIds);

    @Query(value="{'id' : { $in : ?0}}", delete = true)
    void deleteAllById(List<String> componentIds);

}
