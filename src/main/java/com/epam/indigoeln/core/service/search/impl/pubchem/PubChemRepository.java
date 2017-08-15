package com.epam.indigoeln.core.service.search.impl.pubchem;

import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import static com.epam.indigoeln.core.service.search.impl.pubchem.PubChemConst.*;

@Repository
public class PubChemRepository {

    @Autowired
    private RequestSender requestSender;

    public Collection<ProductBatchDetailsDTO> searchByFormula(BatchSearchRequest searchRequest) {
        String searchURI = addBatchesLimit(SEARCH_BY_FORMULA_URI, searchRequest.getBatchesLimit());
        String formula = searchRequest.getStructure().get().getFormula();
        URI uri = new UriTemplate(searchURI).expand(formula);

        RequestEntity<Void> requestEntity = RequestEntity.get(uri).build();
        return requestSender.sendRequest(requestEntity);
    }

    public Collection<ProductBatchDetailsDTO> searchBySimilarity(BatchSearchRequest searchRequest) {
        String searchURI = addBatchesLimit(SEARCH_BY_SIMILARITY_URI, searchRequest.getBatchesLimit());
        URI uri = new UriTemplate(searchURI).expand();

        BatchSearchStructure batchSearchStructure = searchRequest.getStructure().get();
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(SDF_PARAM, batchSearchStructure.getMolfile());
        params.add(THRESHOLD_PARAM, (int) (batchSearchStructure.getSimilarity() * 100));

        RequestEntity<LinkedMultiValueMap<String, Object>> requestEntity = RequestEntity.post(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(params);
        return requestSender.sendRequest(requestEntity);
    }

    public Collection<ProductBatchDetailsDTO> searchByIdentity(BatchSearchRequest searchRequest){
        String searchURI = addBatchesLimit(SEARCH_BY_IDENTITY_URI, searchRequest.getBatchesLimit());
        URI uri = new UriTemplate(searchURI).expand();

        BatchSearchStructure batchSearchStructure = searchRequest.getStructure().get();
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(SDF_PARAM, batchSearchStructure.getMolfile());

        RequestEntity<LinkedMultiValueMap<String, Object>> requestEntity = RequestEntity.post(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(params);
        return requestSender.sendRequest(requestEntity);
    }

    public Collection<ProductBatchDetailsDTO> searchBySubStructure(BatchSearchRequest searchRequest){
        String searchURI = addBatchesLimit(SEARCH_BY_SUBSTRUCTURE_URI, searchRequest.getBatchesLimit());
        URI uri = new UriTemplate(searchURI).expand();

        BatchSearchStructure batchSearchStructure = searchRequest.getStructure().get();
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add(SDF_PARAM, batchSearchStructure.getMolfile());

        RequestEntity<LinkedMultiValueMap<String, Object>> requestEntity = RequestEntity.post(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(params);
        return requestSender.sendRequest(requestEntity);
    }

    private String addBatchesLimit(String searchURI, Optional<Integer> batchesLimit) {
        return batchesLimit.map(limit -> searchURI + "?" + MAX_RECORDS_PARAM + "=" + limit)
                .orElse(searchURI);
    }
}
