package com.epam.indigoeln.core.service.registration;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Compound;
import com.epam.indigoeln.core.model.RegistrationJob;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.registration.*;
import com.epam.indigoeln.core.util.WebSocketUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Service class to work with compound registration services.
 */
@Service
public class RegistrationService {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

    /**
     * Default method name for similarity search.
     */
    private static final String DEFAULT_SIMILARITY = "tanimoto";

    /**
     * List of external registration services.
     */
    private final List<RegistrationRepository> repositories;

    /**
     * ComponentRepository instance for work with components.
     */
    private final ComponentRepository componentRepository;

    /**
     * RegistrationJobRepository instance for work with registration jobs.
     */
    private final RegistrationJobRepository registrationJobRepository;

    /**
     * SimpMessagingTemplate instance for sending messages to websocket.
     */
    private final SimpMessagingTemplate template;

    /**
     * Create a new RegistrationService instance.
     *
     * @param repositories              List of external registration services
     * @param componentRepository       ComponentRepository instance for work with components
     * @param registrationJobRepository RegistrationJobRepository instance for work with registration jobs
     * @param template                  SimpMessagingTemplate instance for sending messages to websocket
     */
    @Autowired
    public RegistrationService(List<RegistrationRepository> repositories,
                               ComponentRepository componentRepository,
                               RegistrationJobRepository registrationJobRepository,
                               SimpMessagingTemplate template) {
        this.repositories = repositories;
        this.componentRepository = componentRepository;
        this.registrationJobRepository = registrationJobRepository;
        this.template = template;
    }

    /**
     * Get external registration services information.
     *
     * @return external registration services information
     */
    public List<RegistrationRepositoryInfo> getRepositoriesInfo() {
        return repositories
                .stream()
                .map(RegistrationRepository::getInfo)
                .collect(Collectors.toList());
    }

    /**
     * Get external registration service by given id.
     *
     * @param id external registration service id
     * @return external registration service with given id
     * @throws RegistrationException if external registration service is not found in initial list
     */
    private RegistrationRepository getRegistrationRepository(String id) throws RegistrationException {
        Optional<RegistrationRepository> optional = repositories
                .stream()
                .filter(r -> r.getInfo().getId().equals(id))
                .findFirst();

        if (!optional.isPresent()) {
            throw new RegistrationException("Unknown repository ID: " + id);
        }

        return optional.get();
    }

    /**
     * Get batch->component map from given component supplier filtered with given predicate.
     *
     * @param supplier  should provide components
     * @param predicate for filtering batches
     * @return batch->component map
     */
    private Map<BatchSummary, Component> getBatches(Supplier<Collection<Component>> supplier,
                                                    Predicate<BatchSummary> predicate) {
        Map<BatchSummary, Component> batchesMap = new HashMap<>();

        supplier.get()
                .forEach(c -> ((BasicDBList) c.getContent().get("batches"))
                        .forEach(b -> {
                            BasicDBObject batch = (BasicDBObject) b;
                            batchesMap.put(new BatchSummary(batch), c);
                        }));

        return batchesMap.entrySet()
                .stream()
                .filter(e -> predicate.test(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Register batches with given batch numbers in external registration service with given ID.
     *
     * @param id               external registration service ID
     * @param fullBatchNumbers batch numbers to register
     * @return registration job ID
     * @throws RegistrationException if batches with given batch numbers cannot be found or already on registration
     */
    public String register(String id, List<String> fullBatchNumbers) throws RegistrationException {
        Map<BatchSummary, Component> batches = getBatches(
                () -> componentRepository.findBatchSummariesByFullBatchNumbers(fullBatchNumbers),
                b -> fullBatchNumbers.contains(b.getFullNbkBatch()));

        if (fullBatchNumbers.size() != batches.size()) {
            Set<String> foundFullNbkBatches = batches.keySet()
                    .stream()
                    .map(BatchSummary::getFullNbkBatch)
                    .collect(Collectors.toSet());

            throw new RegistrationException("Unable to find batches for registration: "
                    + CollectionUtils.subtract(fullBatchNumbers, foundFullNbkBatches));
        }

        Optional<BatchSummary> inProgressOpt = batches.keySet()
                .stream()
                .filter(b -> RegistrationStatus.Status.IN_PROGRESS.toString().equals(b.getRegistrationStatus()))
                .findFirst();

        if (inProgressOpt.isPresent()) {
            throw new RegistrationException("Batch " + inProgressOpt.get().getFullNbkBatch()
                    + " is already on registration.");
        }

        List<Compound> compounds = batches.keySet()
                .stream()
                .map(b -> convert(b.getDelegate()))
                .collect(Collectors.toList());

        String jobId = getRegistrationRepository(id).register(compounds);

        batches.keySet()
                .forEach(b -> {
                    b.setRegistrationStatus(RegistrationStatus.Status.IN_PROGRESS.toString());
                    b.setRegistrationJobId(jobId);
                    b.setRegistrationRepositoryId(id);
                });

        componentRepository.save(new HashSet<>(batches.values()));

        RegistrationJob registrationJob = new RegistrationJob();

        registrationJob.setRegistrationStatus(RegistrationStatus.Status.IN_PROGRESS);
        registrationJob.setRegistrationJobId(jobId);
        registrationJob.setRegistrationRepositoryId(id);
        registrationJob.setHandledBy(WebSocketUtil.getHostName());

        registrationJobRepository.save(registrationJob);

        template.convertAndSend("/topic/registration_status", fullBatchNumbers.stream()
                .collect(Collectors.toMap(fbn -> fbn, fbn -> RegistrationStatus.inProgress())));

        return jobId;
    }

    /**
     * Get registration status for registration job with given ID in external registration service with given ID.
     *
     * @param id    external registration service ID
     * @param jobId registration job ID
     * @return registration status for registration job
     * @throws RegistrationException if external registration service is not found in initial list
     */
    public RegistrationStatus getStatus(String id, String jobId) throws RegistrationException {
        RegistrationStatus registrationStatus = getRegistrationRepository(id).getRegisterJobStatus(jobId);

        if (registrationStatus.getStatus() != RegistrationStatus.Status.IN_PROGRESS) {
            Map<BatchSummary, Component> batches = getBatches(
                    () -> componentRepository.findBatchSummariesByRegistrationJobId(jobId),
                    b -> {
                        String registrationJobId = b.getRegistrationJobId();
                        return registrationJobId != null && StringUtils.equals(registrationJobId, jobId);
                    });

            batches
                    .keySet()
                    .forEach(
                            b -> {
                                b.setRegistrationStatus(registrationStatus.getStatus().toString());

                                if (registrationStatus.getStatus() == RegistrationStatus.Status.PASSED) {
                                    b.setRegistrationDate(registrationStatus.getDate());
                                    b.setCompoundId(registrationStatus.getCompoundNumbers().get(b.getFullNbkBatch()));
                                    b.setConversationalBatchNumber(registrationStatus.getConversationalBatchNumbers()
                                            .get(b.getFullNbkBatch()));
                                }
                            }
                    );

            componentRepository.save(new HashSet<>(batches.values()));
        }

        return registrationStatus;
    }

    /**
     * Get registered compounds for registration job with given ID from external registration service with given ID.
     *
     * @param id    external registration service ID
     * @param jobId registration job ID
     * @return registered compounds for registration job
     * @throws RegistrationException if external registration service is not found in initial list
     */
    public List<Compound> getRegisteredCompounds(String id, String jobId) throws RegistrationException {
        return getRegistrationRepository(id).getRegisteredCompounds(jobId);
    }

    /**
     * Get compound info for compound with given compound number in external registration service with given ID.
     *
     * @param id         external registration service ID
     * @param compoundNo compound number
     * @return compound info for compound
     * @throws RegistrationException if external registration service is not found in initial list
     */
    public List<Compound> getCompoundInfoByCompoundNo(String id, String compoundNo) throws RegistrationException {
        return getRegistrationRepository(id).getCompoundInfoByCompoundNo(compoundNo);
    }

    /**
     * Perform substructure search for given structure in external registration service with given ID.
     *
     * @param id           external registration service ID
     * @param structure    structure to search
     * @param searchOption substructure search options
     * @return found structure IDs
     * @throws RegistrationException if external registration service is not found in initial list
     */
    public List<Integer> searchSubstructure(String id, String structure, String searchOption)
            throws RegistrationException {
        return getRegistrationRepository(id).searchSub(structure, searchOption);
    }

    /**
     * Perform similarity search for given structure in external registration service with given ID.
     *
     * @param id           external registration service ID
     * @param structure    structure to search
     * @param searchOption similarity search options
     * @return found structure IDs
     * @throws RegistrationException if external registration service is not found in initial list
     */
    public List<Integer> searchSimilarity(String id, String structure, String searchOption)
            throws RegistrationException {
        return getRegistrationRepository(id).searchSim(structure,
                DEFAULT_SIMILARITY, Double.parseDouble(searchOption) / 100, (double) 1);
    }

    /**
     * Perform smarts search for given structure in external registration service with given ID.
     *
     * @param id        external registration service ID
     * @param structure structure to search
     * @return found structure IDs
     * @throws RegistrationException if external registration service is not found in initial list
     */
    public List<Integer> searchSmarts(String id, String structure) throws RegistrationException {
        return getRegistrationRepository(id).searchSmarts(structure);
    }

    /**
     * Perform exact search for given structure in external registration service with given ID.
     *
     * @param id           external registration service ID
     * @param structure    structure to search
     * @param searchOption exact search options
     * @return found structure IDs
     * @throws RegistrationException if external registration service is not found in initial list
     */
    public List<Integer> searchExact(String id, String structure, String searchOption) throws RegistrationException {
        return getRegistrationRepository(id).searchExact(structure, searchOption);
    }

    /**
     * Convert batch representation in Mongo to Compound.
     *
     * @param batch batch representation in Mongo
     * @return converted Compound
     */
    private Compound convert(BasicDBObject batch) {
        Compound result = new Compound();

        result.setBatchNo(batch.getString("fullNbkBatch"));
        result.setStructure(((BasicDBObject) batch.get("structure")).getString("molfile"));
        result.setFormula(batch.getString("formula"));
        result.setStereoisomerCode(((BasicDBObject) batch.get("stereoisomer")).getString("name"));
        result.setSaltCode(((BasicDBObject) batch.get("saltCode")).getString("regValue"));

        String saltEq = ((BasicDBObject) batch.get("saltEq")).getString("value");

        if (saltEq != null) {
            try {
                result.setSaltEquivs(Double.parseDouble(saltEq));
            } catch (NumberFormatException e) {
                LOGGER.warn("Unable to parse Salt Eq");
            }
        }

        result.setComment(batch.getString("comments"));

        BasicDBObject healthHazards = (BasicDBObject) batch.get("healthHazards");
        result.setHazardComment(Optional.ofNullable(healthHazards)
                .map(hh -> hh.getString("asString")).orElse(null));

        BasicDBObject storageInstructions = (BasicDBObject) batch.get("storageInstructions");
        result.setStorageComment(Optional.ofNullable(storageInstructions)
                .map(hh -> hh.getString("asString")).orElse(null));

        return result;
    }

    /**
     * Internal batch representation.
     */
    private static class BatchSummary {

        /**
         * Batch representation in Mongo.
         */
        private BasicDBObject delegate;

        /**
         * Create a new BatchSummary instance.
         *
         * @param delegate Batch representation in Mongo
         */
        BatchSummary(BasicDBObject delegate) {
            this.delegate = delegate;
        }

        /**
         * Get batch representation in Mongo.
         *
         * @return batch representation in Mongo
         */
        BasicDBObject getDelegate() {
            return delegate;
        }

        /**
         * Get full nbk batch number from Mongo representation.
         *
         * @return full nbk batch number
         */
        String getFullNbkBatch() {
            return delegate.getString("fullNbkBatch");
        }

        /**
         * Get registration status from Mongo representation.
         *
         * @return registration status
         */
        String getRegistrationStatus() {
            return delegate.getString("registrationStatus");
        }

        /**
         * Set registration status to Mongo representation.
         *
         * @param registrationStatus registration status
         */
        void setRegistrationStatus(String registrationStatus) {
            delegate.put("registrationStatus", registrationStatus);
        }

        /**
         * Get registration job ID from Mongo representation.
         *
         * @return registration job ID
         */
        String getRegistrationJobId() {
            return delegate.getString("registrationJobId");
        }

        /**
         * Set registration job ID to Mongo representation.
         *
         * @param registrationJobId registration job ID
         */
        void setRegistrationJobId(String registrationJobId) {
            delegate.put("registrationJobId", registrationJobId);
        }

        /**
         * Set external registration service ID to Mongo representation.
         *
         * @param registrationRepositoryId external registration service ID
         */
        void setRegistrationRepositoryId(String registrationRepositoryId) {
            delegate.put("registrationRepositoryId", registrationRepositoryId);
        }

        /**
         * Set registration date to Mongo representation.
         *
         * @param registrationDate registration date
         */
        void setRegistrationDate(Date registrationDate) {
            delegate.put("registrationDate", registrationDate);
        }

        /**
         * Set compound ID to Mongo representation.
         *
         * @param compoundId compound ID
         */
        void setCompoundId(String compoundId) {
            delegate.put("compoundId", compoundId);
        }

        /**
         * Set conversational batch number to Mongo representation.
         *
         * @param conversationalBatchNumber conversational batch number
         */
        void setConversationalBatchNumber(String conversationalBatchNumber) {
            delegate.put("conversationalBatchNumber", conversationalBatchNumber);
        }
    }
}