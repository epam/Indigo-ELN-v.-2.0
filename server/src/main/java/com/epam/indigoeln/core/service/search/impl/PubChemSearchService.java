package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.service.search.SearchServiceConstants;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PubChemSearchService implements SearchServiceAPI {

    private static final String URL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/";

    private final Indigo indigo;
    private final RestClient restClient = RestClient.create();
    private ObjectReader compoundsResponseReader;

    {
        compoundsResponseReader = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readerFor(PCCompoundsResponse.class);
    }

    @Override
    public Info getInfo() {
        return new Info(2, "PubChem", false);
    }

    @Override
    public Collection<ProductBatchDetailsDTO> findBatches(BatchSearchRequest searchRequest) {
        Set<String> conditions = new HashSet<>();
        List<String> queryArgs = new ArrayList<>();
        List<String> formArgs = new ArrayList<>();
        searchRequest.getSearchQuery().ifPresent(query -> {
            conditions.add("name/" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        });
        searchRequest.getStructure().ifPresent(structure -> {
            IndigoObject indigoObject = indigo.loadMolecule(structure.getMolfile());
            String smarts = indigoObject.smarts();
            String queryType = switch (structure.getSearchMode()) {
                case SearchServiceConstants.CHEMISTRY_SEARCH_EXACT -> "fastidentity";
                case SearchServiceConstants.CHEMISTRY_SEARCH_SUBSTRUCTURE -> "fastsubstructure";
                case SearchServiceConstants.CHEMISTRY_SEARCH_SIMILARITY -> "fastsimilarity_2d";
                case SearchServiceConstants.CHEMISTRY_SEARCH_MOLFORMULA -> "fastformula";
                default -> throw new IllegalArgumentException("Unknown search mode: " + structure.getSearchMode());
            };
            if (structure.getSearchMode().equals(SearchServiceConstants.CHEMISTRY_SEARCH_SIMILARITY)) {
                queryArgs.add("Threshold=" + structure.getSimilarity());
            }
            conditions.add(queryType + "/smarts");
            formArgs.add("smarts=" + URLEncoder.encode(smarts, StandardCharsets.UTF_8));
        });
        queryArgs.add("MaxRecords=10"); // !!!
        if (conditions.size() != 1) {
            log.error("Invalid search conditions: {}", conditions);
            throw new IllegalArgumentException("Invalid or unsupported search request");
        }
        try {
            String url = URL + conditions.iterator().next() + "/JSON?" + String.join("&", queryArgs);
            System.err.println("!!! url = " + url);
            RestClient.RequestBodySpec request = restClient.post().uri(url);
            if (!formArgs.isEmpty()) {
                request = request.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(String.join("&", formArgs));
            }
            String response = request.retrieve().body(String.class);
            return parseCompounds(response);
        } catch (Exception e) {
            throw new RuntimeException("PubChem search failed: " + e.getMessage(), e);
        }
    }

    List<ProductBatchDetailsDTO> parseCompounds(String responseStr) {
        try {
            PCCompoundsResponse response = compoundsResponseReader.readValue(responseStr);
            return response.compounds().stream()
                    .map(this::parseCompound)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PubChem response: " + e.getMessage(), e);
        }
    }

    private ProductBatchDetailsDTO parseCompound(PCCompound compound) {
        ProductBatchDetailsDTO dto = new ProductBatchDetailsDTO(null, new HashMap<>());
        dto.getDetails().put("molWeight", compound.getPropertyValue("Molecular Weight", null));
        dto.getDetails().put("formula", compound.getPropertyValue("Molecular Formula", null));
        dto.getDetails().put("chemicalName", compound.getPropertyValue("IUPAC Name", "Preferred"));
        dto.getDetails().put("compoundId", "" + compound.id().id().cid());
        return dto;
    }
}

record PCCompoundsResponse (
        @JsonProperty("PC_Compounds")
        List<PCCompound> compounds
) {
}

record PCCompound (
    PCCompoundIDs id,
    List<PCProperty> props
) {
    String getPropertyValue(String label, String name) {
        for (PCProperty prop : props) {
            if (Objects.equals(prop.urn().label(), label) && Objects.equals(prop.urn().name(), name)) {
                return prop.value().sval();
            }
        }
        return null;
    }
}

record PCCompoundIDs (
        PCCompoundID id
) {
}

record PCCompoundID (
        Long cid
) {
}

record PCProperty (
        PCPropertyURN urn,
        PCPropertyValue value
) {
}

record PCPropertyURN (
        String label,
        String name
) {
}

record PCPropertyValue (
        String sval
) {
}
