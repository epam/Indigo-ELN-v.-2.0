package com.epam.indigoeln.core.service.signature;

import com.epam.indigoeln.core.repository.signature.SignatureRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignatureService {

    @Autowired
    private SignatureRepository signatureRepository;

    public String getReasons() {
        return signatureRepository.getReasons();
    }

    public String getStatuses() {
        return signatureRepository.getStatuses();
    }

    public String getFinalStatus() {
        return signatureRepository.getFinalStatus();
    }

    public String getSignatureTemplates() {
        return signatureRepository.getSignatureTemplates(SecurityUtils.getCurrentUser().getUsername());
    }

    public String uploadDocument(String templateId, String fileName, byte[] file) {
        return signatureRepository.uploadDocument(SecurityUtils.getCurrentUser().getUsername(), templateId, fileName, file);
    }

    public String getDocumentInfo(String documentId) {
        return signatureRepository.getDocumentInfo(documentId);
    }

    public String getDocumentsInfo(List<String> documentIds) {
        return signatureRepository.getDocumentsInfo(documentIds);
    }

    public String getDocuments() {
        return signatureRepository.getDocuments(SecurityUtils.getCurrentUser().getUsername());
    }

    public byte[] downloadDocument(String documentId) {
        return signatureRepository.downloadDocument(documentId);
    }
}
