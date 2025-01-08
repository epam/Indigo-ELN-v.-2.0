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
package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.signature.SignatureService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.web.rest.dto.print.PrintRequest;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/signature")
public class SignatureResource {

    @Autowired
    private SignatureService signatureService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Operation(summary = "Returns reasons.")
    @RequestMapping(value = "/reason", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getReasons() {
        return ResponseEntity.ok(signatureService.getReasons());
    }

    @Operation(summary = "Returns statuses.")
    @RequestMapping(value = "/status", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getStatuses() {
        return ResponseEntity.ok(signatureService.getStatuses());
    }

    @Operation(summary = "Returns final statuses.")
    @RequestMapping(value = "/status/final", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFinalStatus() {
        return ResponseEntity.ok(signatureService.getFinalStatus());
    }

    @Operation(summary = "Returns templates.")
    @RequestMapping(value = "/template", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTemplates() {
        return ResponseEntity.ok(signatureService.getSignatureTemplates());
    }

    @Operation(summary = "Sends document to signature.")
    @RequestMapping(value = "/document", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadDocument(
            @Parameter(description = "File name.") @RequestParam("fileName") String fileName,
            @Parameter(description = "Signature template.") @RequestParam("templateId") String templateId,
            @Parameter(description = "Experiment id.") @RequestParam("experimentId") String experimentId,
            @Parameter(description = "Notebook id.") @RequestParam("notebookId") String notebookId,
            @Parameter(description = "Project id.") @RequestParam("projectId") String projectId,
            @Parameter(description = "Print params.") PrintRequest printRequest) throws IOException {

        User user = userService.getUserWithAuthorities();

        return ResponseEntity.ok(signatureService.uploadDocument(projectId, notebookId,
                experimentId, user, templateId, fileName, printRequest));
    }

    @Operation(summary = "Returns signature document info.")
    @RequestMapping(value = "/document/info", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDocumentInfo(
            @Parameter(description = "Document id.") String documentId
    ) {
        return ResponseEntity.ok(signatureService.getDocumentInfo(documentId));
    }

    @Operation(summary = "Returns signature documents info.")
    @RequestMapping(value = "/document/info", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SignatureService.Document>> getDocumentsInfo(
            @Parameter(description = "Documents ids.") List<String> documentIds
    ) throws IOException {
        return ResponseEntity.ok(signatureService.getDocumentsByIds(documentIds));
    }

    @Operation(summary = "Returns all signature documents info.")
    @RequestMapping(value = "/document", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SignatureService.Document>> getDocuments() throws IOException {
        return ResponseEntity.ok(signatureService.getDocumentsByUser());
    }

    @Operation(summary = "Returns signature document content.")
    @RequestMapping(value = "/document/content", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> downloadDocument(
            @Parameter(description = "Document id.") String documentId
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
