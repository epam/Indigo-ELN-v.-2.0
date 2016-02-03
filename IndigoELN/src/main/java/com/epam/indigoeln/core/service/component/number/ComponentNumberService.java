package com.epam.indigoeln.core.service.component.number;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.Collections;
import java.util.OptionalLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.epam.indigoeln.core.model.Experiment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.core.model.ExperimentShort;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;

import static org.springframework.util.ObjectUtils.nullSafeEquals;
import static org.springframework.util.ObjectUtils.nullSafeToString;

@Service
public class ComponentNumberService {

    private static final String FORMAT_EXPERIMENT_NUMBER = "0000";
    private static final String FORMAT_BATCH_NUMBER = "000";
    private static final String PATTERN_NUMERIC = "[0-9]+";
    private static final String FIELD_COMPONENT_TYPE = "component";
    private static final String FIELD_BATCH_NUMBER = "batchNumber";
    private static final String TYPE_PRODUCT_BATCH_DETAILS = "productBatchDetails";

    private final Object batchLock = new Object();
    private final Object experimentLock = new Object();

    @Autowired
    private ExperimentRepository experimentRepository;

    /**
     * Generate next batch number for experiment with given Id
     * Batch number should be unique within experiment and has format "000"
     * If batches absent in experiment "001" value will be returned
     *
     * @param experimentId id of experiment
     * @return next batch number
     */
    public String generateNextBatchNumber(String experimentId) {
        synchronized (batchLock) {
            Experiment experiment = experimentRepository.findOne(experimentId);
            Collection<String> batchNumbers = experiment.getComponents() == null ? Collections.emptyList() :
                    experiment.getComponents().stream().
                    filter(c -> nullSafeEquals(c.getContent().get(FIELD_COMPONENT_TYPE), TYPE_PRODUCT_BATCH_DETAILS)).
                    map(c -> nullSafeToString(c.getContent().get(FIELD_BATCH_NUMBER))).
                    collect(Collectors.toList());

            return generateNextNumber(batchNumbers, FORMAT_BATCH_NUMBER);
        }
    }

    /**
     * Check, that batch with same batch number already exist in experiment
     * @param experimentId id of experiment
     * @param batchNumber batch number
     * @return is batch number already present
     */
    public boolean isBatchNumberExists(String experimentId, String batchNumber) {
        synchronized (batchLock) {
            Experiment experiment = experimentRepository.findOne(experimentId);
            return experiment.getComponents() != null &&
                    experiment.getComponents().stream().anyMatch(c -> nullSafeEquals(c.getContent().get(FIELD_BATCH_NUMBER), batchNumber));
        }
    }

    /**
     * Generate next experiment number for experiment with given Id
     * Experiment number should be unique within single project and has format "0000"
     * If no any experiment exist for given project "0001" value will be returned
     * @param projectId project id
     * @return next experiment number
     */
    public String generateNextExperimentNumber(String projectId) {
        synchronized (experimentLock) {
            Collection<String> projectExperimentNumbers = experimentRepository.findExperimentsByProject(projectId).
                    stream().
                    map(ExperimentShort::getExperimentNumber).
                    collect(Collectors.toList());

            return generateNextNumber(projectExperimentNumbers, FORMAT_EXPERIMENT_NUMBER);
        }
    }

    /**
     * Check, that experiment with same number already exist in project
     * @param projectId id of project
     * @param experimentNumber experiment number
     * @return is experiment number already present
     */
    public boolean isExperimentNumberExists(String projectId, String experimentNumber) {
        synchronized (experimentLock) {
            return experimentRepository.findExperimentsByProject(projectId).
                    stream().anyMatch(c -> nullSafeEquals(c.getExperimentNumber(), experimentNumber));
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
    private String generateNextNumber(Collection<String> existingValues, String numberFormat) {
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
