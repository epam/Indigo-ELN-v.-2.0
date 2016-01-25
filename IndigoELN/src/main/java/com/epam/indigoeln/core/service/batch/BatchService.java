package com.epam.indigoeln.core.service.batch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;

import com.epam.indigoeln.core.model.Batch;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.service.bingodb.BingoDbIntegrationService;
import com.epam.indigoeln.web.rest.dto.BatchDTO;
import com.epam.indigoeln.core.security.AuthoritiesConstants;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.util.PermissionUtils;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.core.util.SequenceNumberGenerationUtil;

@Service
public class BatchService {

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private BingoDbIntegrationService bingoDbService;

    @Autowired
    private UserService userService;

    @Autowired
    CustomDtoMapper dtoMapper;

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
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public Optional<BatchDTO> createBatch(String experimentId, BatchDTO batchDTO) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment == null) return Optional.empty();

        checkPermissions(experiment, UserPermission.UPDATE_ENTITY);
        experiment.setBatches(Optional.ofNullable(experiment.getBatches()).orElse(new ArrayList<>()));

        if(getBatchById(batchDTO.getId(), experiment).isPresent()){
            throw new ValidationException("Batch with the same id already exists");
        }

        Batch batchForSave = convertFromDTO(batchDTO, experiment, null);
        if(batchDTO.getMolfile() != null){ //save new item to BingoDb if molfile is not empty
            batchForSave.setBingoDbId(bingoDbService.addMolecule(batchDTO.getMolfile()));
        }

        experiment.getBatches().add(validateBatchNumber(batchForSave, experiment.getBatches()));
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
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public Optional<BatchDTO> updateBatch(String experimentId, BatchDTO batchDTO) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment == null) return Optional.empty();

        checkPermissions(experiment, UserPermission.UPDATE_ENTITY);

        Optional<Batch> optionalBatch = getBatchById(batchDTO.getId(), experiment);
        return optionalBatch.map(batch -> updateBatchInternal(batchDTO, experiment, batch.getBingoDbId())).
                    orElseThrow(() -> new ValidationException("Batch with the same id does not exist"));
    }

    private Optional<BatchDTO> updateBatchInternal(BatchDTO batchDTO, Experiment experiment, Integer bingoDbId) {
        Batch batchForSave = convertFromDTO(batchDTO, experiment, bingoDbId);
        if(batchDTO.getMolfile() != null) { // if molfile is not empty update or create new item in BingoDB
            batchForSave.setBingoDbId((batchForSave.getBingoDbId() == null) ?
                    bingoDbService.addMolecule(batchDTO.getMolfile()) : // add new molecule
                    bingoDbService.updateMolecule(batchForSave.getBingoDbId(), batchDTO.getMolfile())); //update existing molecule
        } else { //otherwise, delete from BingoDB if any BingoDB item was assigned
            Optional.ofNullable(batchForSave.getBingoDbId()).ifPresent(bingoDbService::deleteMolecule);
            batchForSave.setBingoDbId(null);
        }
        return saveBatch(batchForSave, experiment);
    }

    /**
     * Delete batch by id and experiment id
     * If any BingoDB item is assigned it will be also deleted
     *
     * @param experimentId experiment id
     * @param batchId batch id
     */
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public void deleteBatch(String experimentId, String batchId) {
        Optional.ofNullable(experimentRepository.findOne(experimentId)).ifPresent(experiment ->
            {
                checkPermissions(experiment, UserPermission.UPDATE_ENTITY);
                Optional <Batch> optionalBatch = getBatchById(batchId, experiment);
                optionalBatch.ifPresent(batch -> Optional.ofNullable(batch.getBingoDbId()).ifPresent(bingoDbService::deleteMolecule));
                experiment.getBatches().removeIf(batch -> batchId.equals(batch.getId()));
                experimentRepository.save(experiment);
            }
        );
    }

    /**
     * Get batch details by experiment id and batch id
     * @param experimentId experiment id
     * @param batchId batch id
     * @return result of operation
     */
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public Optional<BatchDTO> getBatch(String experimentId, String batchId) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment == null) return Optional.empty();

        checkPermissions(experiment, UserPermission.READ_ENTITY);

        Optional<Batch> optionalBatch = getBatchById(batchId, experiment);
        return  optionalBatch.map(this::createBatchDTOFromBatch);
    }

    /**
     * Get all batches by experiment id
     * @param experimentId experiment id
     * @return list of experiment batches enriched with molfile parameters
     */
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public Optional<List<BatchDTO>> getAllExperimentBatches(String experimentId) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment == null) return Optional.empty();

        checkPermissions(experiment, UserPermission.READ_ENTITY);

        if(experiment.getBatches() == null) return Optional.of(new ArrayList<>());
        return Optional.of(experiment.getBatches().stream().map(this::createBatchDTOFromBatch).collect(Collectors.toList()));
    }

    /**
     * Save batch to the database
     * @param batchForSave batch for save
     * @param experiment parent experiment
     * @return saved batch DTO
     */
    private Optional<BatchDTO> saveBatch(Batch batchForSave, Experiment experiment) {
        experiment.getBatches().removeIf(batch -> batch.getId().equals(batchForSave.getId()));
        experiment.getBatches().add(batchForSave);
        experiment.getBatches().sort(Comparator.comparing(Batch::getBatchNumber));
        experimentRepository.save(experiment);
        return getBatch(experiment.getId(), batchForSave.getId());
    }

    /**
     *  Check,that no other batches with the same number exists in current experiment
     * @param batch batch to validate
     * @param allBatches all existing batches of experiment
     */
    private Batch validateBatchNumber(Batch batch, List<Batch> allBatches) {
        if(batch.getBatchNumber() == null) {
            throw new ValidationException("The notebook batch number is not specified");
        }

        if(allBatches.stream().anyMatch(b -> b.getBatchNumber().equals(batch.getBatchNumber()) &&
                !b.getId().equals(batch.getId()))) {
            throw new ValidationException(
                    String.format("The notebook batch number '%s' already exists in the system", batch.getBatchNumber())
            );
        }

        return batch;
    }

    /**
     * Get DTO converted from batch and enriched by molfile parameters
     * @param batch batch to be converted
     * @return batch DTO
     */
    private BatchDTO createBatchDTOFromBatch(Batch batch) {
        BatchDTO batchDTO = new BatchDTO(batch);
        if(batch.getBingoDbId() != null) {
            String molfile = bingoDbService.getMolecule(batch.getBingoDbId());
            IndigoObject indigoObjectMolecule = new Indigo().loadMolecule(molfile);
            batchDTO.setMolfile(molfile);
            batchDTO.setMolecularWeight(indigoObjectMolecule.molecularWeight());
            batchDTO.setFormula(indigoObjectMolecule.grossFormula());
        }
        return batchDTO;
    }

    /**
     * Check user possibility to read or modify given experiment
     * @param experiment experiment
     * @param permission permission to check
     */
    private void checkPermissions(Experiment experiment, String permission) {
        if (!PermissionUtils.hasPermissions(userService.getUserWithAuthorities(),
                experiment.getAccessList(), permission)) {
            throw new AccessDeniedException("Current user doesn't have permissions " +
                    "to perform operation for experiment with id = " + experiment.getId());
        }
    }

    private Optional<Batch> getBatchById(String id, Experiment experiment) {
        return experiment.getBatches().stream().filter(b -> b.getId().equals(id)).findFirst();
    }


    /**
     * Create new batch item from DTO
     * If id or batch Number is absent in given DTO it will be generated automatically
     *
     * @param batchDTO DTO
     * @param experiment experiment
     * @param bingoDbId bingo Db id
     *
     * @return batch item filled from batch DTO
     */
    private Batch convertFromDTO(BatchDTO batchDTO, Experiment experiment, Integer bingoDbId) {
        Batch result = dtoMapper.convertFromDTO(batchDTO);
        result.setId(Optional.ofNullable(batchDTO.getId()).orElse(ObjectId.get().toHexString()));
        result.setBingoDbId(bingoDbId);

        List<String> allBatchNumbers = experiment.getBatches().stream().map(Batch::getBatchNumber).collect(Collectors.toList());
        result.setBatchNumber(Optional.ofNullable(batchDTO.getBatchNumber())
                .orElse(SequenceNumberGenerationUtil.generateNextBatchNumber(allBatchNumbers)));
        return result;
    }
}
