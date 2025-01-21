package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.service.search.SearchServiceConstants;
import com.epam.indigoeln.web.rest.dto.search.ProductBatchDetailsDTO;
import com.epam.indigoeln.web.rest.dto.search.request.BatchSearchRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PubChemSearchService implements SearchServiceAPI {

    private static final String URL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/";

    private final Indigo indigo;
    private final IndigoInchi indigoInchi;
    private final IndigoRenderer indigoRenderer;

    private final RestClient restClient = RestClient.create();
    private ObjectReader compoundsResponseReader;

    @Setter(onMethod_ = @Value("${pubchem.maxResults:10}"))
    private int maxResults;

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
        Triple<String, Map<String, Object>, Map<String, Object>> result = prepareURL(searchRequest);
        try {
            String url = result.getLeft() + "?" + formatURLParams(result.getMiddle());
            RestClient.RequestBodySpec request = restClient.post().uri(url);
            if (!result.getRight().isEmpty()) {
                request = request.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(formatURLParams(result.getRight()));
            }
            String response = request.retrieve().body(String.class);
            return parseCompounds(response);
        } catch (Exception e) {
            throw new RuntimeException("PubChem search failed: " + e.getMessage(), e);
        }
    }

    private String formatURLParams(Map<String, Object> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue().toString(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    Triple<String, Map<String, Object>, Map<String, Object>> prepareURL(BatchSearchRequest searchRequest) {
        Set<String> conditions = new HashSet<>();
        Map<String, Object> queryArgs = new LinkedHashMap<>();
        Map<String, Object> formArgs = new LinkedHashMap<>();
        searchRequest.getSearchQuery().ifPresent(query -> {
            conditions.add("name/" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        });
        searchRequest.getStructure().ifPresent(structure -> {
            String queryType = switch (structure.getSearchMode()) {
                case SearchServiceConstants.CHEMISTRY_SEARCH_EXACT -> "fastidentity";
                case SearchServiceConstants.CHEMISTRY_SEARCH_SUBSTRUCTURE -> "fastsubstructure";
                case SearchServiceConstants.CHEMISTRY_SEARCH_SIMILARITY -> "fastsimilarity_2d";
                case SearchServiceConstants.CHEMISTRY_SEARCH_MOLFORMULA -> "fastformula";
                default -> throw new IllegalArgumentException("Unknown search mode: " + structure.getSearchMode());
            };
            if (structure.getSearchMode().equals(SearchServiceConstants.CHEMISTRY_SEARCH_SIMILARITY)) {
                queryArgs.put("Threshold", (int) (structure.getSimilarity() * 100.0f));
            }
            conditions.add(queryType + "/sdf");
            formArgs.put("sdf", structure.getMolfile());
        });
        queryArgs.put("MaxRecords", maxResults);
        if (conditions.size() != 1) {
            log.error("Invalid search conditions: {}", conditions);
            throw new IllegalArgumentException("Invalid or unsupported search request");
        }
        String url = URL + conditions.iterator().next() + "/JSON";
        System.err.println("!!! url = " + url);
        return Triple.of(url, queryArgs, formArgs);
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
        dto.getDetails().put("molWeight", new ProductBatchDetailsDTO.WithValue(compound.getPropertyValue("Molecular Weight", null)));
        dto.getDetails().put("formula", compound.getPropertyValue("Molecular Formula", null));
        dto.getDetails().put("chemicalName", compound.getPropertyValue("IUPAC Name", "Preferred"));
        dto.getDetails().put("compoundId", "" + compound.id().id().cid());
        String inchi = compound.getPropertyValue("InChI", "Standard");
        if (inchi != null) {
            IndigoObject indigoObject = indigoInchi.loadMolecule(inchi);
            byte[] bytes = indigoRenderer.renderToBuffer(indigoObject);
            dto.getDetails().put("structure", new ProductBatchDetailsDTO.WithImage(Base64.getEncoder().encodeToString(bytes)));
        }
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
