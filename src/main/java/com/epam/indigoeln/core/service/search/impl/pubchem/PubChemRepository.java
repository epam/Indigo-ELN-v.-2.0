/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * Provides repository methods for search.
 */
@Repository
class PubChemRepository {

    /**
     * RequestSender instance to sending request.
     */
    @Autowired
    private RequestSender requestSender;

    /**
     * Searches product batch details by formula.
     *
     * @param searchRequest Search request
     * @return Product batch details
     */
    Collection<ProductBatchDetailsDTO> searchByFormula(BatchSearchRequest searchRequest) {
        String searchURI = addBatchesLimit(SEARCH_BY_FORMULA_URI, searchRequest.getBatchesLimit());
        String formula = searchRequest.getStructure().get().getFormula();
        URI uri = new UriTemplate(searchURI).expand(formula);

        RequestEntity<Void> requestEntity = RequestEntity.get(uri).build();
        return requestSender.sendRequest(requestEntity);
    }

    /**
     * Searches product batch details by similarity.
     *
     * @param searchRequest Search request
     * @return Product batch details
     */
    Collection<ProductBatchDetailsDTO> searchBySimilarity(BatchSearchRequest searchRequest) {
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

    /**
     * Searches product batch details by similarity.
     *
     * @param searchRequest Search request
     * @return Product batch details
     */
    Collection<ProductBatchDetailsDTO> searchByIdentity(BatchSearchRequest searchRequest) {
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

    /**
     * Searches product batch details by structure.
     *
     * @param searchRequest searchRequest
     * @return Product batch details
     */
    Collection<ProductBatchDetailsDTO> searchBySubStructure(BatchSearchRequest searchRequest) {
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
