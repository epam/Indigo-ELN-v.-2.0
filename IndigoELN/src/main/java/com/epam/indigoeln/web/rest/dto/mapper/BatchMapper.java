package com.epam.indigoeln.web.rest.dto.mapper;

import org.mapstruct.Mapper;

import com.epam.indigoeln.core.model.Batch;
import com.epam.indigoeln.web.rest.dto.BatchDTO;


@Mapper
public interface BatchMapper {

    Batch batchFromBatchDTO(BatchDTO batchDTO);

    BatchDTO batchDtoFromBatch(Batch batch);
}
