package com.epam.indigoeln.core.service.signature;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.signature.SignatureRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.epam.indigoeln.core.service.exception.DocumentUploadException;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Document> getDocumentsByIds(Collection<String> documentIds) throws IOException {
        final String content = signatureRepository.getDocumentsInfo(documentIds);
        final DocumentsWrapper wrapper = objectMapper.readValue(content, DocumentsWrapper.class);
        return wrapper.getDocuments();
    }

    public List<Document> getDocumentsByUser(com.epam.indigoeln.core.model.User user) throws IOException {
        final String content = signatureRepository.getDocuments(SecurityUtils.getCurrentUser().getUsername());
        if (!StringUtils.isBlank(content)) {
            final DocumentsWrapper wrapper = objectMapper.readValue(content, DocumentsWrapper.class);
            return wrapper.getDocuments();
        } else {
            return Collections.emptyList();
        }
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
        if (!StringUtils.isBlank(info)) {
            int docStatus = objectMapper.readValue(info, JsonNode.class).get("status").asInt();
            return ISSStatus.fromValue(docStatus);
        } else {
            return ISSStatus.CANCELLED;
        }
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

        @JsonCreator
        public static ISSStatus fromValue(Integer value) {
            for (ISSStatus status : ISSStatus.values()) {
                if (status.getValue().equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    private static class DocumentsWrapper {

        @JsonProperty("Documents")
        private List<Document> documents;

        public List<Document> getDocuments() {
            return documents;
        }

        public void setDocuments(List<Document> documents) {
            this.documents = documents;
        }
    }

    public static class Document {

        private String id;

        private ISSStatus status;

        private boolean actionRequired;

        private List<User> witnesses;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public ISSStatus getStatus() {
            return status;
        }

        public void setStatus(ISSStatus status) {
            this.status = status;
        }

        public boolean isActionRequired() {
            return actionRequired;
        }

        public void setActionRequired(boolean actionRequired) {
            this.actionRequired = actionRequired;
        }

        public List<User> getWitnesses() {
            return witnesses;
        }

        public void setWitnesses(List<User> witnesses) {
            this.witnesses = witnesses;
        }
    }

    public static class User {

        private String firstname;

        private String lastname;

        private String comment;

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

}
