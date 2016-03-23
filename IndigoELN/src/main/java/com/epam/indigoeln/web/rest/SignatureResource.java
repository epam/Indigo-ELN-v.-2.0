package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.signature.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/signature")
public class SignatureResource {

    @Autowired
    private SignatureService signatureService;

    @RequestMapping(value = "/reason", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getReasons() {
        return ResponseEntity.ok(signatureService.getReasons());
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getStatuses() {
        return ResponseEntity.ok(signatureService.getStatuses());
    }

    @RequestMapping(value = "/status/final", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFinalStatus() {
        return ResponseEntity.ok(signatureService.getFinalStatus());
    }

    @RequestMapping(value = "/template", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTemplates() {
        return ResponseEntity.ok(signatureService.getSignatureTemplates());
    }

    @RequestMapping(value = "/document", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestParam("templateId") String templateId,
                                                 @RequestParam("file") MultipartFile file) throws IOException {
        //TODO: change state of the experiment
        return ResponseEntity.ok(signatureService.uploadDocument(templateId, file.getOriginalFilename(), file.getBytes()));
    }

    @RequestMapping(value = "/document/info", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDocumentInfo(String documentId) {
        return ResponseEntity.ok(signatureService.getDocumentInfo(documentId));
    }

    @RequestMapping(value = "/document/info", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDocumentsInfo(List<String> documentIds) {
        return ResponseEntity.ok(signatureService.getDocumentsInfo(documentIds));
    }

    @RequestMapping(value = "/document", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDocuments() {
        return ResponseEntity.ok(signatureService.getDocuments());
    }

    @RequestMapping(value = "/document/content", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> downloadDocument(String documentId) {
        return ResponseEntity.ok(signatureService.downloadDocument(documentId));
    }

}
