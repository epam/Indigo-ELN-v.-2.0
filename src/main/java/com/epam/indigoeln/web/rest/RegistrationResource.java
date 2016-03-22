package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Compound;
import com.epam.indigoeln.core.repository.registration.RegistrationException;
import com.epam.indigoeln.core.repository.registration.RegistrationRepositoryInfo;
import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import com.epam.indigoeln.core.service.registration.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
public class RegistrationResource {

    @Autowired
    private RegistrationService registrationService;

    @RequestMapping(value = "/info", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RegistrationRepositoryInfo>> info() {
        return ResponseEntity.ok(registrationService.getRepositoriesInfo());
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Long register(String id, List<String> fullBatchNumbers) throws RegistrationException {
        return registrationService.register(id, fullBatchNumbers);
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationStatus status(String id, Long jobId) throws RegistrationException {
        return registrationService.getStatus(id, jobId);
    }

    @RequestMapping(value = "/compounds", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Compound> compounds(String id, Long jobId) throws RegistrationException {
        return registrationService.getRegisteredCompounds(id, jobId);
    }

    @RequestMapping(value = "/search/substructure", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSubstructure(String id, String structure, String searchOption) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSubstructure(id, structure, searchOption));
    }

    @RequestMapping(value = "/search/similarity", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSimilarity(String id, String structure, String searchOption) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSimilarity(id, structure, searchOption));
    }

    @RequestMapping(value = "/search/smarts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSmarts(String id, String structure) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchSmarts(id, structure));
    }

    @RequestMapping(value = "/search/exact", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchExact(String id, String structure, String searchOption) throws RegistrationException {
        return ResponseEntity.ok(registrationService.searchExact(id, structure, searchOption));
    }

}
