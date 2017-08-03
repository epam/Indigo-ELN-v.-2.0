package com.epam.indigoeln.core.repository.registration.crs;

import com.epam.indigo.crs.classes.CompoundInfo;
import com.epam.indigo.crs.classes.CompoundRegistrationStatus;
import com.epam.indigo.crs.classes.FullCompoundInfo;
import com.epam.indigo.crs.exceptions.CRSException;
import com.epam.indigo.crs.services.registration.BingoRegistration;
import com.epam.indigo.crs.services.search.BingoSearch;
import com.epam.indigoeln.config.crs.CrsProperties;
import com.epam.indigoeln.core.model.Compound;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationRepositoryInfo;
import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CrsRegistrationRepository implements RegistrationRepository {

    private static final String ID = "CRS";
    private static final String NAME = "CRS Service";
    private static final String STATUS_PASSED = "PASSED";
    private static final String STATUS_FAILED = "FAILED";
    private static RegistrationRepositoryInfo INFO = new RegistrationRepositoryInfo(ID, NAME);

    @Autowired
    private CrsProperties crsProperties;
    @Autowired
    private BingoRegistration registration;
    @Autowired
    private BingoSearch search;

    private String getToken() throws RegistrationException {
        try {
            return registration.getTokenHash(crsProperties.getUsername(), crsProperties.getPassword());
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public String register(List<Compound> compounds) throws RegistrationException {
        try {
            return String.valueOf(
                    registration.submitListForRegistration(
                            getToken(),
                            compounds.stream().map(this::convert).collect(Collectors.toList())));
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public RegistrationStatus getRegisterJobStatus(String jobId) throws RegistrationException {
        try {
            CompoundRegistrationStatus status = registration.checkRegistrationStatus(getToken(), Long.valueOf(jobId));
            final RegistrationStatus result = convert(status, new Date());
            if (RegistrationStatus.Status.PASSED.equals(result.getStatus())) {
                final List<FullCompoundInfo> compounds = search.getCompoundByJobId(String.valueOf(jobId));
                compounds.forEach(c -> {
                    result.getCompoundNumbers().put(c.getBatchNumber(), c.getCompoundNumber());
                    result.getConversationalBatchNumbers().put(c.getBatchNumber(), c.getConversationalBatchNumber());
                });
            }
            return result;
        } catch (CRSException e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public List<Compound> getRegisteredCompounds(String jobId) throws RegistrationException {
        try {
            return search
                    .getCompoundByJobId(jobId)
                    .stream()
                    .map(this::convert)
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

    private RegistrationStatus convert(CompoundRegistrationStatus status, Date date) {
        switch (status) {
            case SUCCESSFUL:
                return RegistrationStatus.passed(date);
            case WRONG_TOKEN_DURING_REGISTRATION:
            case WRONG_TOKEN_DURING_CHECK:
                return RegistrationStatus.failed("Wrong token");
            case FAILED:
                return RegistrationStatus.failed();
            default:
                return RegistrationStatus.inProgress();
        }
    }

    public CompoundInfo convert(Compound compound) {
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

    public Compound convert(FullCompoundInfo compoundInfo) {
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

}
