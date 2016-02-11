package com.epam.indigoeln.core.service.component.name;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;

import com.epam.indigoeln.core.service.util.ComponentsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenerateNameService {

    private static final String FORMAT_EXPERIMENT_NUMBER = "0000";
    private static final String FORMAT_BATCH_NUMBER = "000";

    private static final String PATTERN_NUMERIC = "[0-9]+";

    private final Object batchLock = new Object();
    private final Object experimentLock = new Object();

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    /**
     * Generate next batch number for experiment with given Id
     * Batch number should be unique within experiment and has format "000"
     * If batches absent in experiment "001" value will be returned
     *
     * @param experimentSequenceId id of experiment
     * @return next batch number
     */
    public String generateNextBatchNumber(Long experimentSequenceId) {
        synchronized (batchLock) {
            Experiment experiment = experimentRepository.findOneBySequenceId(experimentSequenceId).
                    orElseThrow(() -> EntityNotFoundException.createWithProjectId(experimentSequenceId.toString()));

            List<Component> components = Optional.ofNullable(experiment.getComponents()).orElse(Collections.emptyList());
            return generateNextNumber(ComponentsUtil.extractBatchNumbers(components), FORMAT_BATCH_NUMBER);
        }
    }

    /**
     * Generate next experiment number for notebook with given Id
     * Experiment number should be unique within single notebook and has format "0000"
     * If no any experiment exist for given notebook "0001" value will be returned
     * @param notebookSequenceId sequence notebook id
     * @return next experiment number
     */
    public String generateExperimentName(Long notebookSequenceId) {
        synchronized (experimentLock) {
            Notebook notebook = notebookRepository.findOneBySequenceId(notebookSequenceId).
                    orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookSequenceId.toString()));

            List<Experiment> experiments = Optional.ofNullable(notebook.getExperiments()).orElse(Collections.emptyList());
            List<String> notebookExperimentNumbers = experiments.
                    stream().map(Experiment::getName).
                    collect(Collectors.toList());

            return generateNextNumber(notebookExperimentNumbers, FORMAT_EXPERIMENT_NUMBER);
        }
    }

    /**
     * Generate next numeric  number in defined format
     * Filter existing Values by values, matches numeric format (such as "001", "002", "1234" etc ),
     * get maximum long value of filtered values, increment it and save in output string format
     * Component or Experiment numbers could be changed by user manually. So, numeric format is voluntary.
     * If no any numbers matches to the numeric format found, default "001" value will be returned
     * If received list of components is empty, default "001" value will be returned
     *
     * @param existingValues list of existing values
     * @return next formatted numeric value
     */
    private String generateNextNumber(List<String> existingValues, String numberFormat) {
        Pattern pattern = Pattern.compile(PATTERN_NUMERIC);
        Format formatter = new DecimalFormat(numberFormat);

        OptionalLong maxNumber = existingValues.stream().
                filter(item -> item != null && pattern.matcher(item).matches()).
                mapToLong(Long::parseLong).
                max();

        long nextComponentNumber = maxNumber.isPresent() ? maxNumber.getAsLong() + 1 : 1L;
        return formatter.format(nextComponentNumber);
    }
}
