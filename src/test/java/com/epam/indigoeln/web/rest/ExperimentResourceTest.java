package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.util.AuthUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.mongodb.BasicDBObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static com.epam.indigoeln.core.security.CookieConstants.CSRF_TOKEN_HEADER;
import static org.junit.Assert.*;

public class ExperimentResourceTest extends RestBase {

    @Autowired
    ExperimentRepository experimentRepository;

    @Autowired
    NotebookRepository notebookRepository;

    @Test
    public void getExperiment() throws Exception {
        RequestEntity<Void> requestEntity = RequestEntity.get(new URI("/api/projects/1/notebooks/1/experiments/1"))
                .header(HttpHeaders.COOKIE, cookie)
                .build();

        ResponseEntity<ExperimentDTO> responseEntity = restTemplate.exchange(requestEntity, ExperimentDTO.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ExperimentDTO experimentDTO = responseEntity.getBody();

        assertEquals("1-1-1", experimentDTO.getFullId());
        assertEquals("0001", experimentDTO.getName());
        assertEquals(ExperimentStatus.OPEN, experimentDTO.getStatus());
        assertEquals(3, experimentDTO.getComponents().size());
        assertEquals(1, experimentDTO.getExperimentVersion());
        assertTrue(experimentDTO.isLastVersion());
        assertEquals(AuthUtil.login, experimentDTO.getAuthor().getLogin());
        assertNotNull(experimentDTO.getCreationDate());
        assertNotNull(experimentDTO.getLastEditDate());
        assertNotNull(AuthUtil.login, experimentDTO.getLastModifiedBy().getLogin());
        assertEquals(Long.valueOf(0), experimentDTO.getVersion());
    }

    @Test
    public void createExperiment() throws URISyntaxException {
        ExperimentDTO experimentDTO = new ExperimentDTO();
        experimentDTO.setComponents(new ArrayList<>());
        for (int i = 0; i < 3; i++) {
            ComponentDTO componentDTO = new ComponentDTO();
            componentDTO.setName("component" + i);
            componentDTO.setContent(new BasicDBObject("field", "value"));
            experimentDTO.getComponents().add(componentDTO);
        }

        RequestEntity<ExperimentDTO> requestEntity = RequestEntity.post(new URI("/api/projects/1/notebooks/1/experiments"))
                .header(HttpHeaders.COOKIE, cookie)
                .header(CSRF_TOKEN_HEADER, csrfToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(experimentDTO);

        ResponseEntity<ExperimentDTO> responseEntity = restTemplate.exchange(requestEntity, ExperimentDTO.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        ExperimentDTO result = responseEntity.getBody();

        assertNotNull(result.getFullId());
        assertEquals("0004", result.getName());
        assertEquals(3, result.getComponents().size());
        assertEquals(ExperimentStatus.OPEN, result.getStatus());
        assertEquals(1, result.getExperimentVersion());
        assertTrue(result.isLastVersion());
        assertEquals(AuthUtil.login, result.getAuthor().getLogin());
        assertNotNull(result.getCreationDate());
        assertNotNull(result.getLastEditDate());
        assertNotNull(result.getLastModifiedBy());
        assertEquals(Long.valueOf(0), result.getVersion());

        Experiment savedExperiment = experimentRepository.findOne(result.getFullId());

        assertNotNull(savedExperiment);
        assertEquals(savedExperiment.getName(), result.getName());
        assertEquals(savedExperiment.getComponents().size(), result.getComponents().size());
        assertEquals(savedExperiment.getStatus(), result.getStatus());
        assertEquals(savedExperiment.getExperimentVersion(), result.getExperimentVersion());
        assertEquals(savedExperiment.isLastVersion(), result.isLastVersion());
        assertEquals(savedExperiment.getAuthor().getLogin(), result.getAuthor().getLogin());
        assertTrue(savedExperiment.getCreationDate().isEqual(result.getCreationDate()));
        assertTrue(savedExperiment.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(savedExperiment.getLastModifiedBy().getLogin(), result.getLastModifiedBy().getLogin());
        assertEquals(savedExperiment.getVersion(), result.getVersion());

        Notebook notebook = notebookRepository.findByExperimentId(result.getFullId());
        assertNotNull(notebook);
    }

    @Test
    public void updateExperiment() throws URISyntaxException {
        Experiment experiment = experimentRepository.findOne("1-1-1");
        ExperimentDTO experimentDTO = new ExperimentDTO(experiment);
        experimentDTO.getComponents().forEach(c -> {
            c.setName("new name");
            c.setContent(new BasicDBObject("new field", "new value"));
        });

        RequestEntity<ExperimentDTO> requestEntity = RequestEntity.put(new URI("/api/projects/1/notebooks/1/experiments"))
                .header(HttpHeaders.COOKIE, cookie)
                .header(CSRF_TOKEN_HEADER, csrfToken)
                .body(experimentDTO);

        ResponseEntity<ExperimentDTO> responseEntity = restTemplate.exchange(requestEntity, ExperimentDTO.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ExperimentDTO result = responseEntity.getBody();

        assertEquals(experimentDTO.getFullId(), result.getFullId());
        assertEquals(experimentDTO.getName(), result.getName());
        assertEquals(experimentDTO.getComponents().size(), result.getComponents().size());
        assertEquals(experimentDTO.getStatus(), result.getStatus());
        assertEquals(experimentDTO.getExperimentVersion(), result.getExperimentVersion());
        assertEquals(experimentDTO.isLastVersion(), result.isLastVersion());
        assertEquals(experimentDTO.getAuthor().getLogin(), result.getAuthor().getLogin());
        assertTrue(experimentDTO.getCreationDate().isEqual(result.getCreationDate()));
        assertFalse(experimentDTO.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(AuthUtil.login, result.getLastModifiedBy().getLogin());
        assertEquals(experimentDTO.getVersion() + 1, result.getVersion().longValue());

        Experiment savedExperiment = experimentRepository.findOne(experimentDTO.getFullId());

        assertNotNull(savedExperiment);
        assertEquals(savedExperiment.getName(), result.getName());
        assertEquals(savedExperiment.getComponents().size(), result.getComponents().size());
        assertEquals(savedExperiment.getStatus(), result.getStatus());
        assertEquals(savedExperiment.getExperimentVersion(), result.getExperimentVersion());
        assertEquals(savedExperiment.isLastVersion(), result.isLastVersion());
        assertEquals(savedExperiment.getAuthor().getLogin(), result.getAuthor().getLogin());
        assertTrue(savedExperiment.getCreationDate().isEqual(result.getCreationDate()));
        assertTrue(savedExperiment.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(savedExperiment.getAuthor().getLogin(), result.getLastModifiedBy().getLogin());
        assertEquals(savedExperiment.getVersion(), result.getVersion());

    }

}