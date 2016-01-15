package com.epam.indigoeln.core.service.batch;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.core.model.Batch;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;

@Service
public class BatchServiceImpl implements BatchService {

    private static final String PATTERN_BATCH_NUMBER = "[0-9]+";
    private static final String NUMBER_FORMAT_BATCH_NUMBER = "000";

    @Autowired
    private ExperimentRepository experimentRepository;

    /**
     * Create or update batch of experiment with specified experimentId
     * If id of 'batchForSave' is empty or does not exist new batch will be saved
     * Otherwise existing batch will be updated.
     * Experiment batches list will be sorted by batch number (in natural order) before saving
     *
     * If batch id is not specified, new value will be generated
     * If batch number is not specified, new value will be generated
     * Batch number is unique for single experiment
     * Batch id is unique for all batches in the system
     *
     * @param experimentId id of experiment
     * @param batchForSave batch for save
     * @return saved Batch
     */
    @Override
    public Batch saveBatch(String experimentId, Batch batchForSave) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment.getBatches() == null) {
            experiment.setBatches(new ArrayList<>());
        }

        if(batchForSave.getId() == null) {
            batchForSave.setId(ObjectId.get().toHexString()); //generate new batch id
        } else {
            //remove existing batch with the same id
            experiment.getBatches().removeIf(batchItem -> batchForSave.getId().equals(batchItem.getId()));
        }

        if (batchForSave.getBatchNumber() == null) {
            //generate batch number automatically, if it is not specified
            batchForSave.setBatchNumber(getNextBatchNumber(experiment.getBatches()));
        }

        experiment.getBatches().add(batchForSave);

        //sort batches list by batch number
        experiment.getBatches().sort((Batch b1, Batch b2) -> b1.getBatchNumber().compareTo(b2.getBatchNumber()));

        experimentRepository.save(experiment);
        return batchForSave;
    }

    @Override
    public void deleteBatch(String experimentId, String batchNumber) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        experiment.getBatches().removeIf(batchItem -> batchNumber.equals(batchItem.getBatchNumber()));
        experimentRepository.save(experiment);
    }

    /**
     * Generate next numeric batch number in defined format
     * Filter batches of experiment with batch number defined in numeric format (such as "001", "002", "1234" etc ),
     * get maximum long value of filtered values, increment it and save in output string format
     * Batch numbers could be changed by user manually. So, numeric format is voluntary.
     * If no any batch number matches to the numeric format found, default "001" value will be returned
     * If received list of batches is empty, default "001" value will be returned
     *
     * @param batches list of experiment batches
     * @return next formatted numeric value of batch number
     */
    private String getNextBatchNumber(List<Batch> batches) {
        Pattern pattern = Pattern.compile(PATTERN_BATCH_NUMBER);
        Format formatter = new DecimalFormat(NUMBER_FORMAT_BATCH_NUMBER);

        Stream<String> batchNumbers = batches.stream().map(Batch::getBatchNumber);
        OptionalLong maxNumber = batchNumbers.
                filter(item -> item != null && pattern.matcher(item).matches()).
                mapToLong(Long::parseLong).
                max();

        long nextBatchNumber = maxNumber.isPresent() ? maxNumber.getAsLong() + 1 : 1L;
        return formatter.format(nextBatchNumber);
    }
}
