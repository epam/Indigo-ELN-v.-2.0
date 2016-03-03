package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.repository.registration.RegistrationRepositoryInfo;
import com.epam.indigoeln.core.service.registration.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
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

    @RequestMapping(value = "/search/substructure", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSubstructure(String id, String structure, String searchOption) throws Exception {
        return ResponseEntity.ok(registrationService.searchSubstructure(id, structure, searchOption));
    }

    @RequestMapping(value = "/search/similarity", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSimilarity(String id, String structure, String searchOption) throws Exception {
        return ResponseEntity.ok(registrationService.searchSimilarity(id, structure, searchOption));
    }

    @RequestMapping(value = "/search/smarts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchSmarts(String id, String structure) throws Exception {
        return ResponseEntity.ok(registrationService.searchSmarts(id, structure));
    }

    @RequestMapping(value = "/search/exact", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchExact(String id, String structure, String searchOption) throws Exception {
        return ResponseEntity.ok(registrationService.searchExact(id, structure, searchOption));
    }

}
