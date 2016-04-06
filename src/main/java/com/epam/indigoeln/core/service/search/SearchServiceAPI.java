package com.epam.indigoeln.core.service.search;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequestDTO;

public interface SearchServiceAPI {

    Collection<ProductBatchDetailsDTO> searchByMolecularFormula(String formula, Map options);

    Collection<ProductBatchDetailsDTO> searchBatches(BatchSearchRequestDTO searchRequest);
}
