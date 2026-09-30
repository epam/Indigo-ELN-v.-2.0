package com.epam.indigoeln.compound.service.search.pubchem;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.search.TextSearch;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.compound.service.search.CatalogSearchProvider;
import com.epam.indigoeln.eln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.eln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.eln.model.SampleSource;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.indigoeln.common.exception.InvalidRequestException.fail;
import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@ApplicationScoped
@SuppressWarnings("UnstableApiUsage")
class PubChemCatalogSearchProvider implements CatalogSearchProvider {

    @ConfigProperty(name = "quarkus.rest-client.pubchem.url")
    String baseUrl;

    @Inject
    @RestClient
    PubChemClient pubChemClient;

    @Inject
    PubChemMapper pubChemMapper;

    @Inject
    IndigoAPI indigo;

    @Inject
    CompoundService compoundService;

    // TODO switch to Redis when Redis is added to the stack
    private final RateLimiter rateLimiter = RateLimiter.create(5.0);

    @Override
    public SearchCatalog catalog() {
        return SearchCatalog.PUBCHEM;
    }

    @Override
    public Page<SampleDTO> search(FindSamplesRequest request, Paging paging) {
        if (paging.getPageNoOrDefault() > 0) { // PubChem doesn't support paging
            return Page.of(paging, null, List.of(), false);
        }
        try {
            List<SampleDTO> list = executeQuery(request, paging.getPageSizeOrDefault());
            return Page.of(paging, null, list, false);
        } catch (PubChemException.NotFound e) {
            return Page.of(paging, null, List.of(), false);
        } catch (PubChemException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("PubChem search failed: " + e.getMessage(), e);
        }
    }

    private List<SampleDTO> executeQuery(FindSamplesRequest searchRequest, int pageSize) {
        validate(searchRequest.getCompoundKey() == null, "For PubChem, Compound Key search is not supported");
        validate(searchRequest.getNbkBatchNumber() == null, "For PubChem, Notebook Batch Number search is not supported");
        validate(searchRequest.getCasNumber() == null, "For PubChem, CAS number search is not supported");
        validate(searchRequest.getCompoundKey() == null, "For PubChem, Compound Key search is not supported");
        validate(searchRequest.getSampleKey() == null, "For PubChem, Sample Key search is not supported");
        validate(searchRequest.getMolWeight() == null, "For PubChem, Molecular Weight search is not supported");
        validate(searchRequest.getChemicalName() == null, "For PubChem, Chemical Name search is not supported");
        validate(searchRequest.getCompoundState() == null, "For PubChem, Compound State search is not supported");
        validate(searchRequest.getBatchComment() == null, "For PubChem, Batch Comment search is not supported");
        validate(searchRequest.getHealthHazards() == null, "For PubChem, Health Hazards search is not supported");

        Set<String> conditions = new HashSet<>();
        Map<String, Object> queryParams = new LinkedHashMap<>();
        Map<String, Object> formParams = new LinkedHashMap<>();
        if (searchRequest.getQuickSearch() != null) {
            conditions.add("name");
            formParams.put("name", searchRequest.getQuickSearch().trim().toLowerCase());
        }
        if (searchRequest.getStructure() != null) {
            String queryType = switch (searchRequest.getStructure().type()) {
                case EXACT -> "fastidentity";
                case SUBSTRUCTURE -> "fastsubstructure";
                case SIMILARITY -> "fastsimilarity_2d";
            };
            conditions.add(queryType + "/sdf");
            formParams.put("sdf", searchRequest.getStructure().query());
            queryParams.put("MaxRecords", pageSize);
        }
        if (searchRequest.getMolecularFormula() != null) {
            if (searchRequest.getMolecularFormula() instanceof TextSearch.ExactSearch(String value)) {
                conditions.add("fastformula/" + URLEncoder.encode(MolFormula.normalize(value), StandardCharsets.UTF_8));
                queryParams.put("MaxRecords", pageSize);
            } else {
                fail("For PubChem, Molecular Formula supports only exact search");
            }
        }
        validate(conditions.size() == 1, "For PubChem, only single condition searches are supported");
        URI url = URI.create(baseUrl + conditions.iterator().next());
        log.debug("Search: url={}, formParams={}, queryParams={}", url, formParams, queryParams);
        rateLimiter.acquire();
        List<PubChemResponse.Item> items = pubChemClient.search(url, queryParams, new MultivaluedHashMap<>(formParams)).propertyTable().items();
        items.sort(Comparator.comparing(PubChemResponse.Item::molWeight));
        return pubChemMapper.mapSamples(items);
    }

    @Override
    public CompoundEntity importCompound(SampleDTO sample) {
        IndigoMolecule molecule = indigo.loadMolecule(checkNotNull(sample.getInchi()));
        return compoundService.findOrCreate(molecule, null, null, null, SampleSource.PUBCHEM, sample.getCompoundKey(), sample.getChemicalName());
    }
}
