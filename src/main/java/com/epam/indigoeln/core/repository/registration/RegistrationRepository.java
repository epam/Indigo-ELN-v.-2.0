package com.epam.indigoeln.core.repository.registration;

import java.util.List;

public interface RegistrationRepository {

    RegistrationRepositoryInfo getInfo();

    List<Integer> searchExact(String structure, String searchOption) throws RegistrationException;

    List<Integer> searchSub(String structure, String searchOption) throws RegistrationException;

    List<Integer> searchSmarts(String structure) throws RegistrationException;

    List<Integer> searchSim(String structure, String similarity, Double var3, Double var4) throws RegistrationException;

}
