package com.epam.indigoeln.core.service.search.impl.pubchem;

import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Collections;
import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

@Service
public class PubChemSearchService implements SearchServiceAPI{

    private static final String NAME = "PubChem";

    @Autowired
    private PubChemRepository pubChemRepository;

    @Override
    public Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        if(searchRequest.getStructure().isPresent()) {
            return search(searchRequest);
        }else {
            return Collections.emptyList();
        }
    }

    private Collection<ProductBatchDetailsDTO> search(BatchSearchRequest searchRequest) {
        Collection<ProductBatchDetailsDTO> result;
        switch (searchRequest.getStructure().get().getSearchMode()) {
            case CHEMISTRY_SEARCH_SUBSTRUCTURE:
                result = pubChemRepository.searchBySubStructure(searchRequest);
                break;

            case CHEMISTRY_SEARCH_EXACT:
                result = pubChemRepository.searchByIdentity(searchRequest);
                break;

            case CHEMISTRY_SEARCH_SIMILARITY:
                result = pubChemRepository.searchBySimilarity(searchRequest);
                break;

            case CHEMISTRY_SEARCH_MOLFORMULA:
                result = pubChemRepository.searchByFormula(searchRequest);
                break;

            default:
                result = Collections.emptyList();
        }
        return result;
    }

    @Override
    public Info getInfo() {
        return new SearchServiceAPI.Info(2, NAME, true);
    }
}
