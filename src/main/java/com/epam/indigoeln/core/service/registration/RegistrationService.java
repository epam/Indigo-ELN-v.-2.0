package com.epam.indigoeln.core.service.registration;

import com.epam.indigoeln.core.model.Compound;
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

    private RegistrationRepository getRegistrationRepository(String id) throws RegistrationException {
        Optional<RegistrationRepository> optional = repositories.stream().filter(r -> r.getInfo().getId().equals(id)).findFirst();
        if (!optional.isPresent()) {
            throw new RegistrationException("Unknown repository ID: " + id);
        }
        return optional.get();
    }

    public Long register(String id, List<Compound> compounds) throws RegistrationException {
        return getRegistrationRepository(id).register(compounds);
    }

    public String getStatus(String id, Long jobId) throws RegistrationException {
        return getRegistrationRepository(id).getRegisterJobStatus(jobId);
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

}
