package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

class PubChemSearchServiceTest {

    @Test
    void testParseResponse() throws Exception {
        String responseStr = Resources.toString(Resources.getResource(getClass(), "pubchem-output.json"), StandardCharsets.UTF_8);
        PubChemSearchService service = new PubChemSearchService(null);
        List<ProductBatchDetailsDTO> compounds = service.parseCompounds(responseStr);
        assert compounds.size() == 1;
        ProductBatchDetailsDTO compound = compounds.get(0);
        assert "167497721".equals(compound.getDetails().get("compoundId"));
        assert "607.7".equals(compound.getDetails().get("molWeight"));
        assert "C34H38FN9O".equals(compound.getDetails().get("formula"));
        assert "3-[1-(2-azabicyclo[2.1.1]hexan-5-yl)-7-(3-cyano-2,3-dihydro-1H-inden-4-yl)-4-[3-(dimethylamino)azetidin-1-yl]-6-fluoroimidazo[4,5-c][1,6]naphthyridin-2-yl]-N,N-dimethylpropanamide".equals(compound.getDetails().get("chemicalName"));
    }
}
