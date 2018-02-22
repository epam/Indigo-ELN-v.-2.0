package com.epam.indigoeln.core.service.signature;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.SignatureJob;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.signature.SignatureJobRepository;
import com.epam.indigoeln.core.repository.signature.SignatureRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.epam.indigoeln.core.service.exception.DocumentUploadException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.print.ITextPrintService;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.core.util.WebSocketUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.print.PrintRequest;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
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

/**
 * Provides methods for signature experiment.
 */
@Service
public class SignatureService {

    /**
     * SignatureRepository instance.
     */
    @Autowired
    private SignatureRepository signatureRepository;

    /**
     * ExperimentRepository instance for working with experiment.
     */
    @Autowired
    private ExperimentService experimentService;

    /**
     * ObjectMapper instance for working with json.
     */
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITextPrintService iTextPrintService;

    @Autowired
    private SignatureJobRepository signatureJobRepository;

    public String getReasons() {
        return signatureRepository.getReasons();
    }

    public String getStatuses() {
        return signatureRepository.getStatuses();
    }

    public String getFinalStatus() {
        return signatureRepository.getFinalStatus();
    }

    /**
     * Returns signature templates.
     *
     * @return Signature templates for current user
     */
    public String getSignatureTemplates() {
        return signatureRepository.getSignatureTemplates(SecurityUtils.getCurrentUser().getUsername());
    }

    /**
     * * Uploads document to signature.
     *
     * @param templateId   Template's id
     * @param fileName     File's name
     * @param projectId    Project id
     * @param notebookId   Notebook id
     * @param experimentId Experiment id
     * @param user         User
     * @param printRequest Print params
     * @return Result of uploading
     * @throws IOException if a low-level I/O problem (unexpected end-of-input,
     *                     network error) occurs
     */
    public String uploadDocument(String projectId, String notebookId, String experimentId,
                                 com.epam.indigoeln.core.model.User user, String templateId,
                                 String fileName, PrintRequest printRequest) throws IOException {
        Experiment experiment =
                experimentService.getExperiment(SequenceIdUtil.buildFullId(projectId, notebookId, experimentId));

        if (!PermissionUtil.hasEditorAuthorityOrPermissions(user, experiment.getAccessList(),
                UserPermission.UPDATE_ENTITY)) {
            throw OperationDeniedException.createExperimentUpdateOperation(experiment.getId());
        }
        experiment.setStatus(ExperimentStatus.COMPLETED);
        byte[] bytes = iTextPrintService.generateExperimentPdf(projectId, notebookId, experiment, printRequest, user);
        String result = signatureRepository.uploadDocument(SecurityUtils.getCurrentUser().getUsername(),
                templateId, fileName, bytes);

        // extract uploaded document id
        String documentId = objectMapper.readValue(result, JsonNode.class).get("id").asText();

        if (documentId == null) {
            throw DocumentUploadException.createFailedUploading(experimentId);
        }

        experiment.setDocumentId(documentId);
        experiment.setStatus(ExperimentStatus.SUBMITTED);
        experiment.setSubmittedBy(user);
        experimentService.updateExperimentStatus(experiment);

        // create status checking job
        SignatureJob signatureJob = new SignatureJob();
        signatureJob.setExperimentId(SequenceIdUtil.buildFullId(projectId, notebookId, experimentId));
        signatureJob.setExperimentStatus(ExperimentStatus.SUBMITTED);
        signatureJob.setHandledBy(WebSocketUtil.getHostName());
        signatureJobRepository.save(signatureJob);

        return result;
    }

    /**
     * Returns document's information.
     *
     * @param documentId Document's id
     * @return Document's information
     */
    public String getDocumentInfo(String documentId) {
        return signatureRepository.getDocumentInfo(documentId);
    }

    public List<Document> getDocumentsByIds(Collection<String> documentIds) throws IOException {
        final String content = signatureRepository.getDocumentsInfo(documentIds);
        if (!StringUtils.isBlank(content)) {
            final DocumentsWrapper wrapper = objectMapper.readValue(content, DocumentsWrapper.class);
            return wrapper.getDocuments();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns documents by user's id.
     *
     * @return List with documents
     * @throws IOException if a low-level I/O problem (unexpected end-of-input,
     *                     network error) occurs
     */
    public List<Document> getDocumentsByUser() throws IOException {
        final String content = signatureRepository.getDocuments(SecurityUtils.getCurrentUser().getUsername());
        if (!StringUtils.isBlank(content)) {
            final DocumentsWrapper wrapper = objectMapper.readValue(content, DocumentsWrapper.class);
            return wrapper.getDocuments();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Downloads document.
     *
     * @param documentId Document's id
     * @return Document
     */
    public byte[] downloadDocument(String documentId) {
        return signatureRepository.downloadDocument(documentId);
    }

    public ExperimentStatus updateAndGetExperimentStatus(Experiment experiment)
            throws IOException {
        // check experiment in status Submitted or Signing
        if (ExperimentStatus.SUBMITTED.equals(experiment.getStatus())
                || ExperimentStatus.SINGING.equals(experiment.getStatus())
                || ExperimentStatus.SINGED.equals(experiment.getStatus())) {

            if (experiment.getDocumentId() == null) {
                throw DocumentUploadException.createNullDocumentId(experiment.getId());
            }

            ISSStatus status = getStatus(experiment.getDocumentId());
            final ExperimentStatus expectedStatus = getExperimentStatus(status);

            // update experiment if differ
            if (!expectedStatus.equals(experiment.getStatus())) {

                experiment.setStatus(expectedStatus);

                experimentService.updateExperimentStatus(experiment);

                return expectedStatus;
            }
        }
        return experiment.getStatus();
    }

    /**
     * Check experiment's status on Signature Service and update in DB if changed.
     *
     * @param experimentDTO Experiment
     * @return Experiment's status
     * @throws IOException If there is a low-level I/O problem
     */
    public ExperimentStatus updateAndGetExperimentStatus(ExperimentDTO experimentDTO)
            throws IOException {

        Experiment experiment = experimentService.getExperiment(experimentDTO.getFullId());
        return updateAndGetExperimentStatus(experiment);
    }

    /**
     * Converts signature service status to experiment status.
     *
     * @param status Signature service status
     * @return Experiment status
     */
    public ExperimentStatus getExperimentStatus(SignatureService.ISSStatus status) {

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
        } else if (SignatureService.ISSStatus.SIGNING.equals(status)
                || SignatureService.ISSStatus.WAITING.equals(status)) {
            expectedStatus = ExperimentStatus.SINGING;
        } else if (SignatureService.ISSStatus.SIGNED.equals(status)
                || SignatureService.ISSStatus.ARCHIVING.equals(status)) {
            expectedStatus = ExperimentStatus.SINGED;
        } else if (SignatureService.ISSStatus.ARCHIVED.equals(status)) {
            expectedStatus = ExperimentStatus.ARCHIVED;
        } else {
            expectedStatus = ExperimentStatus.SUBMIT_FAIL;
        }
        return expectedStatus;

    }

    /**
     * Returns signature service status by document's id.
     *
     * @param documentId Document's id
     * @return Signature service status
     * @throws IOException If there is a low-level I/O problem
     */
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
     * Indigo Signature Service statuses.
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

        List<Document> getDocuments() {
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

        private String firstName;

        private String lastName;

        private String comment;

        public String getFirstName() {
            return firstName;
        }

        @JsonProperty("firstname")
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        @JsonProperty("lastname")
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
