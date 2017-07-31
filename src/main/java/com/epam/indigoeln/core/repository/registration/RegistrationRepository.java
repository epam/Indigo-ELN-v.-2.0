package com.epam.indigoeln.core.repository.registration;

import com.epam.indigoeln.core.model.Compound;

import java.util.List;

public interface RegistrationRepository {

    RegistrationRepositoryInfo getInfo();

    String register(List<Compound> compounds) throws RegistrationException;

    RegistrationStatus getRegisterJobStatus(String jobId) throws RegistrationException;

    List<Compound> getRegisteredCompounds(String jobId) throws RegistrationException;

    List<Integer> searchExact(String structure, String searchOption) throws RegistrationException;

    List<Integer> searchSub(String structure, String searchOption) throws RegistrationException;

    List<Integer> searchSmarts(String structure) throws RegistrationException;

    List<Integer> searchSim(String structure, String similarity, Double var3, Double var4) throws RegistrationException;

    List<String> getStructureByCompoundNo(String compoundNumber) throws RegistrationException;

    List<String> getStructureByCasNo(String casNo) throws RegistrationException;

    String getStructureByBatchNo(String batchNo) throws RegistrationException;

    List<Compound> getCompoundInfoByCompoundNo(String compoundNo) throws RegistrationException;

    List<Compound> getCompoundInfoByCasNo(String casNo) throws RegistrationException;

    Compound getCompoundInfoByBatchNo(String batchNo) throws RegistrationException;

}
