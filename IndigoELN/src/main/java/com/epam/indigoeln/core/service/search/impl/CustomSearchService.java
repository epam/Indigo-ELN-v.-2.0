package com.epam.indigoeln.core.service.search.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.core.integration.BingoResult;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.service.bingodb.BingoDbIntegrationService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.errors.CustomParametrizedException;
import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;

import static org.springframework.util.ObjectUtils.nullSafeEquals;
import static java.util.stream.Collectors.toList;
import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

@Service("customSearchService")
public class CustomSearchService implements SearchServiceAPI {

    @Autowired
    private BingoDbIntegrationService bingoDbService;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    /**
     * Find components (batches) by chemical molecular structure
     * @param structure structure of component
     * @param searchOperator search operator (now, Bingo DB supports 'exact', 'similarity', 'substructure' types)
     * @param options search advanced options
     * @return list of batches with received structure
     */
    @Override
    public Collection<ComponentDTO> searchByMolecularStructure(String structure,
                                                               String searchOperator,
                                                               Map options) {
        List<String> bingoIds =
                handleBingoException(bingoDbService.searchMolecule(structure, searchOperator, options)).getSearchResult();

        return bingoIds.isEmpty() ? Collections.emptyList() :
                componentRepository.findBatchesByBingoDbIds(bingoIds).stream().map(ComponentDTO::new).collect(toList());
    }

    /**
     * Find component by full batch number
     * Full batch number expected in format NOTEBOOK_NUMBER(8 digits)-EXPERIMENT_NUMBER(4 digits)-BATCH_NUMBER(3 digits)
     * @param fullBatchNumber full batch number
     * @return result of search
     */
    @Override
    public Optional<ComponentDTO> getComponentInfoByBatchNumber(String fullBatchNumber) {
        Pattern pattern = Pattern.compile(FULL_BATCH_NUMBER_FORMAT);
        if(!pattern.matcher(fullBatchNumber).matches()){ //check, that full batch number received in proper format
            return Optional.empty();
        }

        List<String> parsedNumber = Splitter.on("-").splitToList(fullBatchNumber); //parse full batch number as notebook-experiment-batch number
        String notebookNumber = parsedNumber.get(0);
        String experimentNumber = parsedNumber.get(1);
        String batchNumber = parsedNumber.get(2);

        Optional<ComponentDTO> result = Optional.empty();
        Optional<Notebook> notebook = notebookRepository.findByName(notebookNumber);
        if(notebook.isPresent()){ //if notebook with same number is present
            Optional<Experiment> experiment = getExperimentByNumber(notebook.get().getExperiments(), experimentNumber);
            if(experiment.isPresent()) { //if experiment with same number is present
                result = getBatchByNumber(experiment.get().getComponents(), batchNumber); //try to find batch with number
            }
        }
        return result;
    }

    private BingoResult handleBingoException(BingoResult result) {
        if(!result.isSuccess()) {
            throw new CustomParametrizedException(result.getErrorMessage());
        }
        return result;
    }

    /**
     * find experiment by number
     */
    private Optional<Experiment> getExperimentByNumber(Collection<Experiment> experiments, String number) {
        return experiments == null ? Optional.empty() :
                experiments.stream().filter(experiment -> nullSafeEquals(experiment.getName(), number)).findAny();
    }

    /**
     * Find batch by batch number
     * find component with content type 'batchDetails' and batch number equals to expected
     */
    private Optional<ComponentDTO> getBatchByNumber(Collection<Component> components, String number) {
        return  components == null ? Optional.empty() :
                components.stream().filter(c ->
                        c.getContent() != null &&
                        nullSafeEquals("batchDetails", c.getContent().getString("component")) &&
                        nullSafeEquals(number, c.getContent().getString("batchNumber"))
                ).findAny().map(ComponentDTO::new);
    }
}
