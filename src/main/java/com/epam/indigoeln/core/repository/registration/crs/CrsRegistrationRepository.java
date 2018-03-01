/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import com.epam.indigoeln.core.service.calculation.CalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CrsRegistrationRepository implements RegistrationRepository {

    private static final String ID = "CRS";
    private static final String NAME = "CRS Service";
    private static final String STATUS_PASSED = "PASSED";
    private static final String STATUS_FAILED = "FAILED";
    private static RegistrationRepositoryInfo info = new RegistrationRepositoryInfo(ID, NAME);

    @Autowired
    private CrsProperties crsProperties;
    @Autowired
    private BingoRegistration registration;
    @Autowired
    private BingoSearch search;
    @Autowired
    private CalculationService calculationService;

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
            CompoundRegistrationStatus status = registration.checkRegistrationStatus(getToken(), Long.parseLong(jobId));
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
    public List<Integer> searchSim(String structure, String similarity, Double var3, Double var4)
            throws RegistrationException {
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
        return info;
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
        CompoundInfo compoundInfo = new CompoundInfo();

        compoundInfo.setBatchNumber(compound.getBatchNo());
        compoundInfo.setCasNumber(compound.getCasNo());
        compoundInfo.setComments(compound.getComment());
        compoundInfo.setData(compound.getStructure());
        compoundInfo.setHazardComments(compound.getHazardComment());
        compoundInfo.setSaltCode(compound.getSaltCode());
        compoundInfo.setSaltEquivalents(compound.getSaltEquivs());
        compoundInfo.setStereoIsomerCode(compound.getStereoisomerCode());
        compoundInfo.setStorageComments(compound.getStorageComment());

        return compoundInfo;

    }

    public Compound convert(FullCompoundInfo compoundInfo) {
        if (compoundInfo == null) {
            return null;
        }

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

        Map<String, String> map = calculationService.getMolecularInformation(compoundInfo.getData(),
                Optional.of(compoundInfo.getSaltCode()), Optional.of((float)compoundInfo.getSaltEquivalents()));

        compound.setFormula(map.get("molecularFormula"));

        if (compoundInfo.getRegistrationStatus() == CompoundRegistrationStatus.SUCCESSFUL) {
            compound.setRegistrationStatus(STATUS_PASSED);
        } else {
            compound.setRegistrationStatus(STATUS_FAILED);
        }

        return compound;
    }

}