package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.signature.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/signature")
public class SignatureResource {

    @Autowired
    private SignatureService signatureService;


    @RequestMapping(value = "/template", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTemplates() {
        return ResponseEntity.ok(signatureService.getSignatureTemplates());
    }

    @RequestMapping(value = "/document", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestParam("templateId") String templateId,
                                                 @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(signatureService.uploadDocument(templateId, file.getOriginalFilename(), file.getBytes()));
    }

}
