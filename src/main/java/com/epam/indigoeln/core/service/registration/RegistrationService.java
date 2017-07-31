package com.epam.indigoeln.core.service.registration;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Compound;
import com.epam.indigoeln.core.model.RegistrationJob;
import com.epam.indigoeln.core.repository.component.ComponentRepository;
import com.epam.indigoeln.core.repository.registration.*;
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

@Service
public class RegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

    private static final String DEFAULT_SIMILARITY = "tanimoto";

    private final List<RegistrationRepository> repositories;
    private final ComponentRepository componentRepository;
    private final RegistrationJobRepository registrationJobRepository;
    private final SimpMessagingTemplate template;

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

    public List<RegistrationRepositoryInfo> getRepositoriesInfo() {
        return repositories
                .stream()
                .map(RegistrationRepository::getInfo)
                .collect(Collectors.toList());
    }

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

    private Map<BatchSummary, Component> getBatches(Supplier<Collection<Component>> supplier, Predicate<BatchSummary> predicate) {
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

    public String register(String id, List<String> fullBatchNumbers) throws RegistrationException {
        Map<BatchSummary, Component> batches = getBatches(
                () -> componentRepository.findBatchSummariesByFullBatchNumbers(fullBatchNumbers),
                b -> fullBatchNumbers.contains(b.getFullNbkBatch()));

        if (fullBatchNumbers.size() != batches.size()) {
            Set<String> foundFullNbkBatches = batches.keySet()
                    .stream()
                    .map(BatchSummary::getFullNbkBatch)
                    .collect(Collectors.toSet());

            throw new RegistrationException("Unable to find batches for registration: " +
                    CollectionUtils.subtract(fullBatchNumbers, foundFullNbkBatches));
        }

        Optional<BatchSummary> inProgressOpt = batches.keySet()
                .stream()
                .filter(b -> RegistrationStatus.Status.IN_PROGRESS.toString().equals(b.getRegistrationStatus()))
                .findFirst();

        if (inProgressOpt.isPresent()) {
            throw new RegistrationException("Batch " + inProgressOpt.get().getFullNbkBatch() + " is already on registration.");
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

        registrationJobRepository.save(registrationJob);

        template.convertAndSend("/topic/registration_status", fullBatchNumbers.stream().collect(Collectors.toMap(fbn -> fbn, fbn -> RegistrationStatus.inProgress())));

        return jobId;
    }

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
                                    b.setConversationalBatchNumber(registrationStatus.getConversationalBatchNumbers().get(b.getFullNbkBatch()));
                                }
                            }
                    );

            componentRepository.save(new HashSet<>(batches.values()));
        }

        return registrationStatus;
    }

    public List<Compound> getRegisteredCompounds(String id, String jobId) throws RegistrationException {
        return getRegistrationRepository(id).getRegisteredCompounds(jobId);
    }

    public List<Compound> getCompoundInfoByCompoundNo(String id, String compoundNo) throws RegistrationException {
        return getRegistrationRepository(id).getCompoundInfoByCompoundNo(compoundNo);
    }

    public List<Integer> searchSubstructure(String id, String structure, String searchOption) throws RegistrationException {
        return getRegistrationRepository(id).searchSub(structure, searchOption);
    }

    public List<Integer> searchSimilarity(String id, String structure, String searchOption) throws RegistrationException {
        return getRegistrationRepository(id).searchSim(structure, DEFAULT_SIMILARITY, Double.parseDouble(searchOption) / 100, (double) 1);
    }

    public List<Integer> searchSmarts(String id, String structure) throws RegistrationException {
        return getRegistrationRepository(id).searchSmarts(structure);
    }

    public List<Integer> searchExact(String id, String structure, String searchOption) throws RegistrationException {
        return getRegistrationRepository(id).searchExact(structure, searchOption);
    }

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
        result.setHazardComment(Optional.ofNullable(healthHazards).map(hh -> hh.getString("asString")).orElse(null));

        BasicDBObject storageInstructions = (BasicDBObject) batch.get("storageInstructions");
        result.setStorageComment(Optional.ofNullable(storageInstructions).map(hh -> hh.getString("asString")).orElse(null));

        return result;
    }

    private class BatchSummary {

        private BasicDBObject delegate;

        BatchSummary(BasicDBObject delegate) {
            this.delegate = delegate;
        }

        BasicDBObject getDelegate() {
            return delegate;
        }

        String getFullNbkBatch() {
            return delegate.getString("fullNbkBatch");
        }

        String getRegistrationStatus() {
            return delegate.getString("registrationStatus");
        }

        void setRegistrationStatus(String registrationStatus) {
            delegate.put("registrationStatus", registrationStatus);
        }

        String getRegistrationJobId() {
            return delegate.getString("registrationJobId");
        }

        void setRegistrationJobId(String registrationJobId) {
            delegate.put("registrationJobId", registrationJobId);
        }

        void setRegistrationRepositoryId(String registrationRepositoryId) {
            delegate.put("registrationRepositoryId", registrationRepositoryId);
        }

        void setRegistrationDate(Date registrationDate) {
            delegate.put("registrationDate", registrationDate);
        }

        void setCompoundId(String compoundId) {
            delegate.put("compoundId", compoundId);
        }

        void setConversationalBatchNumber(String conversationalBatchNumber) {
            delegate.put("conversationalBatchNumber", conversationalBatchNumber);
        }
    }
}
