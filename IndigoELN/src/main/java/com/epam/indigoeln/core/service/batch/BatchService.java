package com.epam.indigoeln.core.service.batch;

import com.epam.indigoeln.web.rest.dto.BatchDTO;

import java.util.Optional;

public interface BatchService {

    BatchDTO createBatch(String experimentId, BatchDTO batch);

    BatchDTO updateBatch(String experimentId, BatchDTO batch);

    void deleteBatch(String experimentId, String batchId);

    Optional<BatchDTO> getBatch(String experimentId, String batchId);
}
