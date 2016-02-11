package com.epam.indigoeln.core.repository.component;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.epam.indigoeln.core.model.Component;

import static com.epam.indigoeln.core.model.Component.TYPE_PRODUCT_BATCH_DETAILS;
import static com.epam.indigoeln.core.model.Component.FIELD_BINGO_ID;

public interface ComponentRepository extends MongoRepository<Component, String> {

    @Query(value="{ 'name' :' " + TYPE_PRODUCT_BATCH_DETAILS + "', 'content." + FIELD_BINGO_ID + "' : { $in : ?0 } }")
    List<Component> findBatchesByBingoDbIds(List<String> bingoIds);

    @Query(value="{'id' : { $in : ?0}}", delete = true)
    void deleteAllById(List<String> componentIds);

}
