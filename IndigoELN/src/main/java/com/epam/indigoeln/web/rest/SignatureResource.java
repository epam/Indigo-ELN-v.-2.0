package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.exception.DocumentUploadException;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Api
@RestController
@RequestMapping("/api/signature")
public class SignatureResource {

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @ApiOperation(value = "Returns reasons.", produces = "application/json")
    @RequestMapping(value = "/reason", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getReasons() {
        return ResponseEntity.ok(signatureService.getReasons());
    }

    @ApiOperation(value = "Returns statuses.", produces = "application/json")
    @RequestMapping(value = "/status", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getStatuses() {
        return ResponseEntity.ok(signatureService.getStatuses());
    }

    @ApiOperation(value = "Returns final statuses.", produces = "application/json")
    @RequestMapping(value = "/status/final", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFinalStatus() {
        return ResponseEntity.ok(signatureService.getFinalStatus());
    }

    @ApiOperation(value = "Returns templates.", produces = "application/json")
    @RequestMapping(value = "/template", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTemplates() {
        return ResponseEntity.ok(signatureService.getSignatureTemplates());
    }

    @ApiOperation(value = "Sends document to signature.", produces = "application/json")
    @RequestMapping(value = "/document", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadDocument(
            @ApiParam("File name.") @RequestParam("fileName") String fileName,
            @ApiParam("Signature template.") @RequestParam("templateId") String templateId,
            @ApiParam("Experiment id.") @RequestParam("experimentId") String experimentId,
            @ApiParam("Notebook id.") @RequestParam("notebookId") String notebookId,
            @ApiParam("Project id.") @RequestParam("projectId") String projectId) throws IOException {

        // upload file to indigo signature service
        File file = FileUtils.getFile(FileUtils.getTempDirectory(), fileName);
        String result = signatureService.uploadDocument(templateId, fileName, FileUtils.readFileToByteArray(file));

        // extract uploaded document id
        String documentId = objectMapper.readValue(result, JsonNode.class).get("id").asText();

        if (documentId == null) {
            throw DocumentUploadException.createFailedUploading(experimentId);
        }

        // set document id to experiment and update status
        User user = userService.getUserWithAuthorities();
        ExperimentDTO experimentDto = experimentService.getExperiment(projectId, notebookId, experimentId, user);
        experimentDto.setDocumentId(documentId);
        experimentDto.setStatus(ExperimentStatus.SUBMITTED);
        experimentDto.setSubmittedBy(user);
        experimentService.updateExperiment(projectId, notebookId, experimentDto, user);

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Returns signature document info.", produces = "application/json")
    @RequestMapping(value = "/document/info", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDocumentInfo(
            @ApiParam("Document id.") String documentId
        ) {
        return ResponseEntity.ok(signatureService.getDocumentInfo(documentId));
    }

    @ApiOperation(value = "Returns signature documents info.", produces = "application/json")
    @RequestMapping(value = "/document/info", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SignatureService.Document>> getDocumentsInfo(
            @ApiParam("Documents ids.") List<String> documentIds
        ) throws IOException {
        return ResponseEntity.ok(signatureService.getDocumentsByIds(documentIds));
    }

    @ApiOperation(value = "Returns all signature documents info.", produces = "application/json")
    @RequestMapping(value = "/document", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SignatureService.Document>> getDocuments() throws IOException {
        return ResponseEntity.ok(signatureService.getDocumentsByUser());
    }

    @ApiOperation(value = "Returns signature document content.", produces = "application/json")
    @RequestMapping(value = "/document/content", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadDocument(
            @ApiParam("Document id.") String documentId
        ) throws IOException {

        final String info = signatureService.getDocumentInfo(documentId);
        if (!StringUtils.isBlank(info)) {
            String documentName = objectMapper.readValue(info, JsonNode.class).get("documentName").asText();
            HttpHeaders headers = HeaderUtil.createAttachmentDescription(documentName);
            final byte[] data = signatureService.downloadDocument(documentId);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(inputStream));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
