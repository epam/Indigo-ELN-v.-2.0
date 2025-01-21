package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigo.Indigo;
import com.epam.indigoeln.config.bingo.IndigoProvider;
import com.epam.indigoeln.core.service.search.SearchServiceConstants;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchStructure;
import com.google.common.io.Resources;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class PubChemSearchServiceTest {

    public static final String MOLFILE = "\n" +
            "  Ketcher  1212521462D 1   1.00000     0.00000     0\n" +
            "\n" +
            "  8  8  0     0  0            999 V2000\n" +
            "   -0.5000    1.5388    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   -1.3090    0.9511    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   -1.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   -1.5878   -0.8090    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "    0.0000    0.0000    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "    0.3090    0.9511    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "    1.2601    1.2601    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "    0.5878   -0.8090    0.0000 Br  0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "  1  2  1  0  0  0  0\n" +
            "  2  3  1  0  0  0  0\n" +
            "  3  4  2  0  0  0  0\n" +
            "  3  5  1  0  0  0  0\n" +
            "  5  6  1  0  0  0  0\n" +
            "  6  1  1  0  0  0  0\n" +
            "  6  7  2  0  0  0  0\n" +
            "  5  8  1  0  0  0  0\n" +
            "M  END\n";
    IndigoProvider indigoProvider = new IndigoProvider();
    Indigo indigo = indigoProvider.indigo("");
    PubChemSearchService service = new PubChemSearchService(indigo, indigoProvider.inchi(indigo), indigoProvider.renderer(indigo));

    {
        service.setMaxResults(10);
    }

    @Test
    void testQuickSearch() {
        BatchSearchRequest request = new BatchSearchRequest();
        request.setSearchQuery("aspirin");
        Triple<String, Map<String, Object>, Map<String, Object>> result = service.prepareURL(request);
        assert "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/aspirin/JSON".equals(result.getLeft());
        assert Map.of("MaxRecords", 10).equals(result.getMiddle());
        assert Map.of().equals(result.getRight());
    }

    @Test
    void testSubstructureSearch() {
        BatchSearchRequest request = new BatchSearchRequest();
        request.setStructure(new BatchSearchStructure());
        request.getStructure().get().setMolfile(MOLFILE);
        request.getStructure().get().setSearchMode(SearchServiceConstants.CHEMISTRY_SEARCH_SUBSTRUCTURE);
        Triple<String, Map<String, Object>, Map<String, Object>> result = service.prepareURL(request);
        assert "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/fastsubstructure/sdf/JSON".equals(result.getLeft());
        assert Map.of("MaxRecords", 10).equals(result.getMiddle());
        assert Map.of("sdf", MOLFILE).equals(result.getRight());
    }

    @Test
    void testRealSubstructureSearch() {
        BatchSearchRequest request = new BatchSearchRequest();
        request.setStructure(new BatchSearchStructure());
        request.getStructure().get().setMolfile(MOLFILE);
        request.getStructure().get().setSearchMode(SearchServiceConstants.CHEMISTRY_SEARCH_SUBSTRUCTURE);
        Collection<ProductBatchDetailsDTO> result = service.findBatches(request);
        assert !result.isEmpty();
    }

    @Test
    void testRealExactSearch() {
        BatchSearchRequest request = new BatchSearchRequest();
        request.setStructure(new BatchSearchStructure());
        request.getStructure().get().setMolfile(MOLFILE);
        request.getStructure().get().setSearchMode(SearchServiceConstants.CHEMISTRY_SEARCH_EXACT);
        Collection<ProductBatchDetailsDTO> result = service.findBatches(request);
        assert !result.isEmpty();
    }

    @Test
    void testRealSimilatirySearch() {
        BatchSearchRequest request = new BatchSearchRequest();
        request.setStructure(new BatchSearchStructure());
        request.getStructure().get().setMolfile(MOLFILE);
        request.getStructure().get().setSearchMode(SearchServiceConstants.CHEMISTRY_SEARCH_SIMILARITY);
        request.getStructure().get().setSimilarity(0.9f);
        Collection<ProductBatchDetailsDTO> result = service.findBatches(request);
        assert !result.isEmpty();
    }

    @Test
    void testParseResponse() throws Exception {
        String responseStr = Resources.toString(Resources.getResource(getClass(), "pubchem-output.json"), StandardCharsets.UTF_8);
        List<ProductBatchDetailsDTO> compounds = service.parseCompounds(responseStr);
        assert compounds.size() == 1;
        ProductBatchDetailsDTO compound = compounds.get(0);
        assert "167497721".equals(compound.getDetails().get("compoundId"));
        assert new ProductBatchDetailsDTO.WithValue("607.7").equals(compound.getDetails().get("molWeight"));
        assert "C34H38FN9O".equals(compound.getDetails().get("formula"));
        assert "3-[1-(2-azabicyclo[2.1.1]hexan-5-yl)-7-(3-cyano-2,3-dihydro-1H-inden-4-yl)-4-[3-(dimethylamino)azetidin-1-yl]-6-fluoroimidazo[4,5-c][1,6]naphthyridin-2-yl]-N,N-dimethylpropanamide".equals(compound.getDetails().get("chemicalName"));
    }
}
