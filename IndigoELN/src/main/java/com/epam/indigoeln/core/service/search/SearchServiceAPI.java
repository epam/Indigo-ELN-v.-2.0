package com.epam.indigoeln.core.service.search;

import java.util.Collection;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;

public interface SearchServiceAPI {

    Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest);

}
