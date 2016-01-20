package com.epam.indigoeln.core.service.batch;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.validation.ValidationException;

import com.epam.indigoeln.web.rest.dto.BatchDTO;
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
     * Create new batch of experiment with specified experimentId
     * Experiment batches list will be sorted by batch number (in natural order) before saving
     *
     * If batch id is not specified, new value will be generated
     * If batch number is not specified, new value will be generated
     *
     * Batch number is unique for single experiment
     * Batch id is unique for all batches in the system
     *
     * @param experimentId id of experiment
     * @param batchDTO batch for save
     * @return saved Batch
     */
    @Override
    public BatchDTO createBatch(String experimentId, BatchDTO batchDTO) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment.getBatches() == null) {
            experiment.setBatches(new ArrayList<>());
        }

        Batch batchForSave = new Batch();
        batchForSave.setId(batchDTO.getId() != null ? batchDTO.getId() : ObjectId.get().toHexString());
        //if batch number is not specified, new value will be generated
        batchForSave.setBatchNumber(batchDTO.getBatchNumber() != null ? batchDTO.getBatchNumber() : getNextBatchNumber(experiment.getBatches()));

        return saveBatch(batchForSave, experiment);
    }

    /**
     * Update existing batch of experiment with specified experimentId
     * Experiment batches list will be sorted by batch number (in natural order) before saving
     *
     * Batch id and batch Number should be specified
     *
     * Batch number is unique for single experiment
     * Batch id is unique for all batches in the system
     *
     * @param experimentId id of experiment
     * @param batchDTO batch for save
     * @return saved Batch
     */
    @Override
    public BatchDTO updateBatch(String experimentId, BatchDTO batchDTO) {
        Experiment experiment = experimentRepository.findOne(experimentId);

        Batch batchForSave = new Batch();
        batchForSave.setId(batchDTO.getId());
        batchForSave.setBatchNumber(batchDTO.getBatchNumber());

        return saveBatch(batchForSave, experiment);
    }

    @Override
    public void deleteBatch(String experimentId, String batchId) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        experiment.getBatches().removeIf(batchItem -> batchId.equals(batchItem.getId()));
        experimentRepository.save(experiment);
    }


    @Override
    public Optional<BatchDTO> getBatch(String experimentId, String batchId) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        return  experiment != null ?
                experiment.getBatches().stream().filter(b -> b.getId().equals(batchId)).map(BatchDTO::new).findFirst() :
                Optional.empty();
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

    /**
     *  Check,that no other batches with the same number exists in current experiment
     * @param batch batch to validate
     * @param allBatches all existing batches of experiment
     */
    private void validateBatchNumber(Batch batch, List<Batch> allBatches) {
        if(batch.getBatchNumber() == null) {
            throw new ValidationException("The notebook batch number is not specified");
        }

        if(allBatches.stream().anyMatch(b -> b.getBatchNumber().equals(batch.getBatchNumber()) &&
                !b.getId().equals(batch.getId()))) {
            throw new ValidationException(
                    String.format("The notebook batch number '%s' already exists in the system", batch.getBatchNumber())
            );
        }
    }

    private BatchDTO saveBatch(Batch batchForSave, Experiment experiment) {
        //validate batch number for uniqueness
        validateBatchNumber(batchForSave, experiment.getBatches());

        //remove existing batch with the same id
        experiment.getBatches().removeIf(batchItem -> batchForSave.getId().equals(batchItem.getId()));
        experiment.getBatches().add(batchForSave);

        //sort batches list by batch number
        experiment.getBatches().sort((Batch b1, Batch b2) -> b1.getBatchNumber().compareTo(b2.getBatchNumber()));
        experimentRepository.save(experiment);
        return new BatchDTO(batchForSave);
    }
}
