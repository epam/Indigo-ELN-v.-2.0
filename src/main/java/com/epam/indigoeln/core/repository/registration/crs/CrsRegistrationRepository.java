package com.epam.indigoeln.core.repository.registration.crs;

import com.epam.indigo.crs.classes.CompoundInfo;
import com.epam.indigo.crs.classes.CompoundRegistrationStatus;
import com.epam.indigo.crs.classes.FullCompoundInfo;
import com.epam.indigo.crs.exceptions.CRSException;
import com.epam.indigo.crs.services.registration.BingoRegistration;
import com.epam.indigo.crs.services.search.BingoSearch;
import com.epam.indigoeln.core.model.Compound;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationRepositoryInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Repository
public class CrsRegistrationRepository implements RegistrationRepository {

    private static final String ID = "CRS";
    private static final String NAME = "CRS Service";
    private static RegistrationRepositoryInfo INFO = new RegistrationRepositoryInfo(ID, NAME);

    private static final String STATUS_PASSED = "PASSED";
    private static final String STATUS_FAILED = "FAILED";

    @Value("${crs.service.username}")
    private String username;

    @Value("${crs.service.password}")
    private String password;

    @Autowired
    private BingoRegistration registration;

    @Autowired
    private BingoSearch search;

    private String getToken() throws RegistrationException {
        try {
            return registration.getTokenHash(username, password);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public Long register(List<Compound> compounds) throws RegistrationException {
        try {
            return registration.submitListForRegistration(getToken(), compounds.stream().map(this::convert)
                    .collect(Collectors.toList()));
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public String getRegisterJobStatus(long jobId) throws RegistrationException {
        try {
            CompoundRegistrationStatus status = registration.checkRegistrationStatus(getToken(), jobId);
            return convert(status);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Compound> getRegisteredCompounds(long jobId) throws RegistrationException {
        try {
            return search.getCompoundByJobId(Long.toString(jobId)).stream().map(this::convert)
                    .collect(Collectors.toList());
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Integer> searchSim(String structure, String similarity, Double var3, Double var4) throws RegistrationException {
        try {
            return search.searchSim(structure, similarity, var3, var4);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Integer> searchSmarts(String structure) throws RegistrationException {
        try {
            return search.searchSmarts(structure);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Integer> searchSub(String structure, String searchOption) throws RegistrationException {
        try {
            return search.searchSub(structure, searchOption);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Integer> searchExact(String structure, String searchOption) throws RegistrationException {
        try {
            return search.searchExact(structure, searchOption);
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<String> getStructureByCompoundNo(String compoundNumber) throws RegistrationException {
        return getCompoundInfoByCompoundNo(compoundNumber).stream().map(Compound::getStructure).
                collect(Collectors.toList());
    }

    @Override
    public List<String> getStructureByCasNo(String casNo) throws RegistrationException {
        return getCompoundInfoByCasNo(casNo).stream().map(Compound::getStructure).
                collect(Collectors.toList());
    }

    @Override
    public String getStructureByBatchNo(String batchNo) throws RegistrationException {
        Compound compound = getCompoundInfoByBatchNo(batchNo);
        return compound == null ? null : compound.getStructure();
    }

    @Override
    public List<Compound> getCompoundInfoByCompoundNo(String compoundNo) throws RegistrationException {
        try {
            return search.getCompoundByNumber(compoundNo).stream().map(this::convert).
                    collect(Collectors.toList());
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Compound> getCompoundInfoByCasNo(String casNo) throws RegistrationException {
        try {
            return search.getCompoundByCasNumber(casNo).stream().map(this::convert).
                    collect(Collectors.toList());
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public Compound getCompoundInfoByBatchNo(String batchNo) throws RegistrationException {
        try {
            return convert(search.getCompoundByBatchNumber(batchNo));
        } catch (Exception e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public RegistrationRepositoryInfo getInfo() {
        return INFO;
    }

    private String convert(CompoundRegistrationStatus status) {
        switch (status) {
            case SUCCESSFUL:
                return STATUS_PASSED;
            case WRONG_TOKEN_DURING_REGISTRATION:
            case WRONG_TOKEN_DURING_CHECK:
                return STATUS_FAILED;
            default:
                return status.toString();
        }
    }

    private CompoundInfo convert(Compound compound) {
        CompoundInfo info = new CompoundInfo();

        info.setBatchNumber(compound.getBatchNo());
        info.setCasNumber(compound.getCasNo());
        info.setComments(compound.getComment());
        info.setData(compound.getStructure());
        info.setHazardComments(compound.getHazardComment());
        info.setSaltCode(compound.getSaltCode());
        info.setSaltEquivalents(compound.getSaltEquivs());
        info.setStereoIsomerCode(compound.getStereoisomerCode());
        info.setStorageComments(compound.getStorageComment());

        return info;

    }

    private Compound convert(FullCompoundInfo compoundInfo) {
        if (compoundInfo == null)
            return null;

        Compound compound = new Compound();

        compound.setStructure(compoundInfo.getData());
        compound.setCompoundNo(compoundInfo.getCompoundNumber());
        compound.setConversationalBatchNo(compoundInfo.getConversationalBatchNumber());
        compound.setBatchNo(compoundInfo.getBatchNumber());
        compound.setCasNo(compoundInfo.getCasNumber());
        compound.setSaltCode(compoundInfo.getSaltCode());
        compound.setSaltEquivs(compoundInfo.getSaltEquivalents());
        compound.setComment(compoundInfo.getComments());
        compound.setHazardComment(compoundInfo.getHazardComments());
        compound.setStereoisomerCode(compoundInfo.getStereoIsomerCode());
        compound.setStorageComment(compoundInfo.getStorageComments());

        if (compoundInfo.getRegistrationStatus() == CompoundRegistrationStatus.SUCCESSFUL) {
            compound.setRegistrationStatus(STATUS_PASSED);
        } else {
            compound.setRegistrationStatus(STATUS_FAILED);
        }

        return compound;
    }

    private class RegistrationStatusTask implements Runnable {

        private final Map<Long, CompletableFuture<Pair<Long, String>>> jobsMap;

        public RegistrationStatusTask() {
            this.jobsMap = Collections.synchronizedMap(new HashMap<>());
        }

        @Override
        public void run() {
            synchronized (jobsMap) {
                Set<Long> finishedJobsIds = new HashSet<>();
                for (Long jobId : jobsMap.keySet()) {
                    CompletableFuture<Pair<Long, String>> future = jobsMap.get(jobId);
                    try {
                        CompoundRegistrationStatus status = registration.checkRegistrationStatus(getToken(), jobId);
                        if (status != CompoundRegistrationStatus.NOT_REGISTERED_YET) {
                            // Status was updated, we need to unschedule task
                            future.complete(Pair.of(jobId, convert(status)));
                            finishedJobsIds.add(jobId);
                        }
                    } catch (RegistrationException | CRSException e) {
                        future.completeExceptionally(e);
                        finishedJobsIds.add(jobId);
                    }
                }
                finishedJobsIds.stream().forEach(jobsMap::remove);
            }
        }

        public void addJob(Long jobId, CompletableFuture<Pair<Long, String>> future) {
            jobsMap.put(jobId, future);
        }
    }

}
