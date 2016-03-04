package com.epam.indigoeln.core.service.search.impl;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.service.search.SearchServiceAPI;
import com.epam.indigoeln.core.service.util.ComponentsUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

@Service("customSearchService")
public class CustomSearchService implements SearchServiceAPI {

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private BingoService bingoService;

    /**
     * Find components (batches) by chemical molecular structure
     *
     * @param structure      structure of component
     * @param searchOperator search operator (now, Bingo DB supports 'exact', 'similarity', 'substructure' types)
     * @param options        search advanced options
     * @return list of batches with received structure
     */
    @Override
    public Collection<ComponentDTO> searchByMolecularStructure(String structure,
                                                               String searchOperator,
                                                               Map options) {

        // TODO Need to replace StringUtils.EMPTY with correct options for Bingo

        List<String> bingoIds;

        switch (searchOperator) {
            case CHEMISTRY_SEARCH_SUBSTRUCTURE:
                bingoIds = bingoService.searchMoleculeSub(structure, StringUtils.EMPTY)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                break;
            case CHEMISTRY_SEARCH_EXACT:
                bingoIds = bingoService.searchMoleculeExact(structure, StringUtils.EMPTY)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                break;
            case CHEMISTRY_SEARCH_SIMILARITY:
                bingoIds = bingoService.searchMoleculeSim(structure, Float.valueOf(options.get("min").toString()), Float.valueOf(options.get("max").toString()), StringUtils.EMPTY)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                break;
            case CHEMISTRY_SEARCH_MOLFORMULA:
                bingoIds = bingoService.searchMoleculeMolFormula(structure, StringUtils.EMPTY)
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                break;
            default:
                bingoIds = new ArrayList<>();
                break;
        }

        return bingoIds.isEmpty() ? Collections.emptyList() : componentRepository.findBatchesByBingoDbIds(bingoIds).stream().map(ComponentDTO::new).collect(toList());
    }

    /**
     * Find component by full batch number
     * Full batch number expected in format NOTEBOOK_NUMBER(8 digits)-EXPERIMENT_NUMBER(4 digits)-BATCH_NUMBER(3 digits)
     *
     * @param fullBatchNumber full batch number
     * @return result of search
     */
    @Override
    public Optional<ComponentDTO> getComponentInfoByBatchNumber(String fullBatchNumber) {
        Pattern pattern = Pattern.compile(FULL_BATCH_NUMBER_FORMAT);
        if (!pattern.matcher(fullBatchNumber).matches()) { //check, that full batch number received in proper format
            return Optional.empty();
        }

        List<String> parsedNumber = Splitter.on("-").splitToList(fullBatchNumber); //parse full batch number as notebook-experiment-batch number
        String notebookNumber = parsedNumber.get(0);
        String experimentNumber = parsedNumber.get(1);
        String batchNumber = parsedNumber.get(2);

        Optional<ComponentDTO> result = Optional.empty();
        Optional<Notebook> notebook = notebookRepository.findByName(notebookNumber);
        if (notebook.isPresent()) { //if notebook with same number is present
            Optional<Experiment> experiment = getExperimentByNumber(notebook.get().getExperiments(), experimentNumber);
            if (experiment.isPresent()) { //if experiment with same number is present
                result = ComponentsUtil.getBatchByNumber(experiment.get().getComponents(), batchNumber); //try to find batch with number
            }
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

}
