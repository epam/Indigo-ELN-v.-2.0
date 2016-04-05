package com.epam.indigoeln.core.service.signature;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.signature.SignatureRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.epam.indigoeln.core.service.exception.DocumentUploadException;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
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
    private ExperimentRepository experimentRepository;

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

    public ExperimentStatus checkExperimentStatus(Experiment experiment)
            throws IOException {
        return checkExperimentStatus(new ExperimentDTO(experiment));
    }

    /**
     * Check experiment's status on Signature Service and update in DB if changed
     */
    public ExperimentStatus checkExperimentStatus(ExperimentDTO experimentDTO)
            throws IOException {
        // check experiment in status Submitted or Signing
        if (ExperimentStatus.SUBMITTED.equals(experimentDTO.getStatus()) ||
                ExperimentStatus.SINGING.equals(experimentDTO.getStatus()) ||
                ExperimentStatus.SINGED.equals(experimentDTO.getStatus())) {

            if (experimentDTO.getDocumentId() == null) {
                throw DocumentUploadException.createNullDocumentId(experimentDTO.getId());
            }

            SignatureService.ISSStatus status = getStatus(experimentDTO.getDocumentId());
            final ExperimentStatus expectedStatus = getExperimentStatus(status);

            // update experiment if differ
            if (!expectedStatus.equals(experimentDTO.getStatus())) {
                final Experiment experiment = experimentRepository.findOne(experimentDTO.getFullId());
                experiment.setStatus(expectedStatus);
                experimentRepository.save(experiment);
                return expectedStatus;
            }
        }
        return experimentDTO.getStatus();
    }

    private ExperimentStatus getExperimentStatus(SignatureService.ISSStatus status) {

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
        if (SignatureService.ISSStatus.SUBMITTED.equals(status)) {
            expectedStatus = ExperimentStatus.SUBMITTED;
        } else if (SignatureService.ISSStatus.SIGNING.equals(status) || SignatureService.ISSStatus.WAITING.equals(status)) {
            expectedStatus = ExperimentStatus.SINGING;
        } else if (SignatureService.ISSStatus.SIGNED.equals(status) || SignatureService.ISSStatus.ARCHIVING.equals(status)) {
            expectedStatus = ExperimentStatus.SINGED;
        } else if (SignatureService.ISSStatus.ARCHIVED.equals(status)) {
            expectedStatus = ExperimentStatus.ARCHIVED;
        } else {
            expectedStatus = ExperimentStatus.SUBMIT_FAIL;
        }
        return expectedStatus;

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
