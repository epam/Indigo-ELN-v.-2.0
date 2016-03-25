package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.exception.DocumentUploadException;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.sequenceid.SequenceIdService;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.TreeNodeDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ExperimentResource.URL_MAPPING)
public class ExperimentResource {

    static final String URL_MAPPING = "/api/projects/{projectId:[\\d]+}/notebooks/{notebookId:[\\d]+}/experiments";
    private static final String PATH_ID = "/{id:[\\d]+}";
    private static final String ENTITY_NAME = "Experiment";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentResource.class);

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    private SequenceIdService sequenceIdService;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * GET  /notebooks/:notebookId/experiments -> Returns all experiments, which author is current User<br/> ?????????
     * GET  /notebooks/:notebookId/experiments -> Returns all experiments of specified notebook for <b>current user</b>
     * for tree representation according to his User permissions
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllExperimentsByPermissions(@PathVariable String projectId,
                                                            @PathVariable String notebookId) {
        User user = userService.getUserWithAuthorities();
        if (notebookId == null) {
            LOGGER.debug("REST request to get all experiments, which author is current user");
            Collection<ExperimentDTO> experiments = experimentService.getExperimentsByAuthor(user);
            return ResponseEntity.ok(experiments); //TODO May be use DTO only with required fields for Experiment?
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("REST request to get all experiments of notebook: {} " +
                        "according to user permissions", notebookId);
            }
            List<TreeNodeDTO> result = experimentService.getAllExperimentTreeNodes(projectId, notebookId, user);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * GET  /notebooks/:notebookId/experiments/all -> Returns all experiments of specified notebook
     * without checking for User permissions
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TreeNodeDTO>> getAllExperiments(@PathVariable String projectId,
                                                               @PathVariable String notebookId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REST request to get all experiments of notebook: {} " +
                    "without checking for permissions", notebookId);
        }
        List<TreeNodeDTO> result = experimentService.getAllExperimentTreeNodes(projectId, notebookId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /experiments/:id -> Returns experiment with specified id according to User permissions
     */
    @RequestMapping(value = PATH_ID, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> getExperiment(
            @PathVariable String projectId,
            @PathVariable String notebookId,
            @PathVariable String id) throws IOException {
        LOGGER.debug("REST request to get experiment: {}", id);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO experimentDTO = experimentService.getExperiment(projectId, notebookId, id, user);
//        checkExperimentStatus(projectId, notebookId, experimentDTO, user);
        return ResponseEntity.ok(experimentService.getExperiment(projectId, notebookId, id, user));
    }

    /**
     * GET  /experiments/:id/batch_number -> Returns batch_number
     */
    @RequestMapping(value = PATH_ID + "/batch_number", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> getNotebookBatchNumber(
            @PathVariable String projectId,
            @PathVariable String notebookId,
            @PathVariable String id,
            @RequestParam(required = false, name = "latest") String clientLatestBatchNumber) {
        String batchNumber = sequenceIdService.getNotebookBatchNumber(projectId, notebookId, id, clientLatestBatchNumber);
        LOGGER.debug("REST request to get batch number: {}", batchNumber);
        return ResponseEntity.ok(ImmutableMap.of("batchNumber", batchNumber));
    }

    /**
     * POST  /experiments?:notebookId -> Creates experiment with OWNER's permissions for current User
     * as child for specified Notebook
     */
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> createExperiment(@RequestBody ExperimentDTO experimentDTO,
                                                          @PathVariable String projectId,
                                                          @PathVariable String notebookId)
            throws URISyntaxException {
        LOGGER.debug("REST request to create experiment: {} for notebook: {}", experimentDTO, notebookId);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO savedExperimentDTO = experimentService.createExperiment(experimentDTO, projectId, notebookId, user);
        HttpHeaders headers = HeaderUtil.createEntityCreateAlert(ENTITY_NAME, savedExperimentDTO.getId());
        return ResponseEntity.created(new URI("/api/projects/" + projectId + "/notebooks/" + notebookId + "/experiments" + experimentDTO.getId()))
                .headers(headers).body(savedExperimentDTO);
    }

    /**
     * PUT  /experiments/:id -> Updates experiment according to User permissions
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentDTO> updateExperiment(@RequestBody ExperimentDTO experimentDTO,
                                                          @PathVariable String projectId,
                                                          @PathVariable String notebookId) {
        LOGGER.debug("REST request to update experiment: {}", experimentDTO);
        User user = userService.getUserWithAuthorities();
        ExperimentDTO updatedExperimentDTO = experimentService.updateExperiment(projectId, notebookId, experimentDTO, user);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedExperimentDTO.getId());
        return ResponseEntity.ok().headers(headers).body(updatedExperimentDTO);
    }

    /**
     * DELETE  /experiments/:id -> Removes experiment with specified id
     */
    @RequestMapping(value = PATH_ID, method = RequestMethod.DELETE)
    public ResponseEntity deleteExperiment(@PathVariable String id,
                                              @PathVariable String projectId,
                                              @PathVariable String notebookId) {
        LOGGER.debug("REST request to remove experiment: {}", id);
        experimentService.deleteExperiment(id, projectId, notebookId);
        HttpHeaders headers = HeaderUtil.createEntityDeleteAlert(ENTITY_NAME, id);
        return ResponseEntity.ok().headers(headers).build();
    }

    /**
     *  Check experiment's status on Signature Service and update in DB if changed
     */
    private ExperimentDTO checkExperimentStatus(String projectId, String notebookId, ExperimentDTO experimentDTO, User user)
            throws IOException {
        // check experiment in status Submitted or Signing
        if (ExperimentStatus.SUBMITTED.equals(experimentDTO.getStatus()) ||
                ExperimentStatus.SINGING.equals(experimentDTO.getStatus()) ||
                ExperimentStatus.SINGED.equals(experimentDTO.getStatus())) {

            if (experimentDTO.getDocumentId() == null) {
                throw DocumentUploadException.createNullDocumentId(experimentDTO.getId());
            }

            // get document's status
            String info = signatureService.getDocumentInfo(experimentDTO.getDocumentId());
            int docStatus = objectMapper.readValue(info, JsonNode.class).get("status").asInt();

            // match statuses
            // Indigo Signature Service statuses:
//            ------------------------------
//             Signature(Id)    |  IndigoELN
//            ------------------------------
//            SUBMITTED(1) -> SUBMITTED
//            SIGNING(2)   -> SIGNING
//            SIGNED(3)    -> SIGNED
//            REJECTED(4)  -> SUBMIT_FAILED
//            WAITING(5)   -> SIGNING
//            CANCELLED(6) -> SUBMIT_FAILED
//            ARCHIVING(7) -> SIGNED
//            ARCHIVED(8)  -> ARCHIVE
//            ------------------------------
            ExperimentStatus expectedStatus;
            if (docStatus == 1) {
                expectedStatus = ExperimentStatus.SUBMITTED;
            } else if (docStatus == 2 || docStatus == 5) {
                expectedStatus = ExperimentStatus.SINGING;
            } else if (docStatus == 3 || docStatus == 7) {
                expectedStatus = ExperimentStatus.SINGED;
            } else if (docStatus == 8) {
                expectedStatus = ExperimentStatus.ARCHIVED;
            } else {
                expectedStatus = ExperimentStatus.SUBMIT_FAIL;
            }

            // update experiment if differ
            if (!expectedStatus.equals(experimentDTO.getStatus())) {
                experimentDTO.setStatus(expectedStatus);
                experimentDTO = experimentService.updateExperiment(projectId, notebookId, experimentDTO, user);
            }
        }
        return experimentDTO;
    }


}