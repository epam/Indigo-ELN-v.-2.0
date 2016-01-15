package com.epam.indigoeln.core.service.batch;

import com.epam.indigoeln.core.model.Batch;

public interface BatchService {

    Batch saveBatch(String experimentId, Batch batch);

    void deleteBatch(String experimentId, String batchNumber);
}
