package com.epam.indigoeln.core.service.registration;

import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationRepository;
import com.epam.indigoeln.core.repository.registration.RegistrationRepositoryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private static final String DEFAULT_SIMILARITY = "tanimoto";

    @Autowired
    private List<RegistrationRepository> repositories;

    public List<RegistrationRepositoryInfo> getRepositoriesInfo() {
        return repositories.stream().map(RegistrationRepository::getInfo).collect(Collectors.toList());
    }

    private Optional<RegistrationRepository> getRegistrationRepository(String id) {
        return repositories.stream().filter(r -> r.getInfo().getId().equals(id)).findFirst();
    }

    public List<Integer> searchSubstructure(String id, String structure, String searchOption) throws RegistrationException {
        Optional<RegistrationRepository> repository = getRegistrationRepository(id);
        if (!repository.isPresent()) {
            throw new RegistrationException("Unknown repository ID: " + id);
        }
        return repository.get().searchSub(structure, searchOption);
    }

    public List<Integer> searchSimilarity(String id, String structure, String searchOption) throws RegistrationException {
        Optional<RegistrationRepository> repository = getRegistrationRepository(id);
        if (!repository.isPresent()) {
            throw new RegistrationException("Unknown repository ID: " + id);
        }
        return repository.get().searchSim(structure, DEFAULT_SIMILARITY, Double.parseDouble(searchOption) / 100, (double) 1);
    }

    public List<Integer> searchSmarts(String id, String structure) throws RegistrationException {
        Optional<RegistrationRepository> repository = getRegistrationRepository(id);
        if (!repository.isPresent()) {
            throw new RegistrationException("Unknown repository ID: " + id);
        }
        return repository.get().searchSmarts(structure);
    }

    public List<Integer> searchExact(String id, String structure, String searchOption) throws RegistrationException {
        Optional<RegistrationRepository> repository = getRegistrationRepository(id);
        if (!repository.isPresent()) {
            throw new RegistrationException("Unknown repository ID: " + id);
        }
        return repository.get().searchExact(structure, searchOption);
    }

}
