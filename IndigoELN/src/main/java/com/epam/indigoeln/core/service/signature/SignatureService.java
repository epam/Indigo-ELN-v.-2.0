package com.epam.indigoeln.core.service.signature;

import com.epam.indigoeln.core.repository.signature.SignatureRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SignatureService {

    @Autowired
    private SignatureRepository signatureRepository;

    @Autowired
    private ObjectMapper objectMapper;

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

    public ISSStatus getStatus(String documentId) throws IOException {
        // get document's status
        String info = signatureRepository.getDocumentInfo(documentId);
        int docStatus = objectMapper.readValue(info, JsonNode.class).get("status").asInt();
        return ISSStatus.fromValue(docStatus);
    }

    /**
     * Indigo Signature Service statuses
     */
    public enum ISSStatus {
        SUBMITTED(1),
        SIGNING(2),
        SIGNED(3),
        REJECTED(4),
        WAITING(5),
        CANCELLED(6),
        ARCHIVING(7),
        ARCHIVED(8);

        private Integer value;

        ISSStatus(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public static ISSStatus fromValue(Integer value) {
            for (ISSStatus status : ISSStatus.values()) {
                if (status.getValue().equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException();
        }
    }


}
