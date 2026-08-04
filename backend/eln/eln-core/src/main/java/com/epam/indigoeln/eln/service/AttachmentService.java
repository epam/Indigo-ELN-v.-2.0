package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.AttachmentMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.repository.*;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.common.util.ContentDispositionUtil.generateContentDisposition;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class AttachmentService {

    @Inject
    UserService userService;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    NotebookRepository notebookRepository;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ACLService aclService;
    @Inject
    ProjectAttachmentRepository projectAttachmentRepository;
    @Inject
    NotebookAttachmentRepository notebookAttachmentRepository;
    @Inject
    ExperimentAttachmentRepository experimentAttachmentRepository;
    @Inject
    AttachmentMapper attachmentMapper;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    ProjectService projectService;
    @Inject
    NotebookService notebookService;

    public List<AttachmentDTO> createProjectAttachment(UUID projectId, FileUpload file, boolean useMutation) {
        return createProjectAttachment(projectId, file.fileName(), readFile(file), useMutation);
    }

    public List<AttachmentDTO> createProjectAttachment(UUID projectId, String filename, byte[] content, boolean useMutation) {
        ProjectEntity project = projectRepository.loadAndLock(projectId);
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
        ProjectAttachment attachment = doCreateAttachment(new ProjectAttachment(), filename, content, projectAttachmentRepository);
        if (useMutation) {
            projectService.applyMutation(project, new ProjectMutation.CreateProjectAttachment(attachment.getId()));
        } else {
            doAddAttachment(project, attachment);
        }
        return attachmentMapper.attachmentToDTOList(project.getAttachments());
    }

    public List<AttachmentDTO> createNotebookAttachment(UUID notebookId, FileUpload file, boolean useMutation) {
        return createNotebookAttachment(notebookId, file.fileName(), readFile(file), useMutation);
    }

    public List<AttachmentDTO> createNotebookAttachment(UUID notebookId, String filename, byte[] content, boolean useMutation) {
        NotebookEntity notebook = notebookRepository.loadAndLock(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
        NotebookAttachment attachment = doCreateAttachment(new NotebookAttachment(), filename, content, notebookAttachmentRepository);
        if (useMutation) {
            notebookService.applyMutation(notebook, new NotebookMutation.CreateNotebookAttachment(attachment.getId()));
        } else {
            doAddAttachment(notebook, attachment);
        }
        return attachmentMapper.attachmentToDTOList(notebook.getAttachments());
    }

    public List<AttachmentDTO> createExperimentAttachment(UUID experimentId, FileUpload file, @Nullable Boolean useMutation) {
        return createExperimentAttachment(experimentId, file.fileName(), readFile(file), useMutation);
    }

    public List<AttachmentDTO> createExperimentAttachment(UUID experimentId, String filename, byte[] content, @Nullable Boolean useMutation) {
        ExperimentEntity experiment = experimentRepository.loadAndLock(experimentId);
        createExperimentAttachment(experiment, filename, content, useMutation);
        return attachmentMapper.attachmentToDTOList(experiment.getAttachments());
    }

    public ExperimentAttachment createExperimentAttachment(ExperimentEntity experiment, String filename, byte[] content, @Nullable Boolean useMutation) {
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
        ExperimentAttachment attachment = doCreateAttachment(new ExperimentAttachment(), filename, content, experimentAttachmentRepository);
        if (useMutation == Boolean.TRUE) {
            experimentModelService.applyMutation(experiment, new ExperimentMutation.CreateExperimentAttachment(attachment.getId()));
        } else if (useMutation == Boolean.FALSE) {
            doAddAttachment(experiment, attachment);
        }
        return attachment;
    }

    public <P extends BaseEntity & WithAttachments<A>, A extends AbstractAttachment<P>> void doAddAttachment(P parent, A attachment) {
        attachment.setParent(parent);
        parent.getAttachments().add(attachment);
    }

    private byte[] readFile(FileUpload file) {
        try {
            return Files.readAllBytes(file.filePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read attachment content", e);
        }
    }

    private <A extends AbstractAttachment<?>> A doCreateAttachment(A attachment, String filename, byte[] content, BaseRepository<A> repository) {
        attachment.setName(filename);
        attachment.setSize((long) content.length);
        attachment.setDeleted(false);
        attachment.setContent(content);

        updateDates(attachment, userService.getCurrentUserEntity());
        repository.persist(attachment);
        return attachment;
    }

    public Response downloadProjectAttachment(UUID projectId, UUID attachmentId) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.VIEW_PROJECTS);
        ProjectAttachment attachment = projectAttachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, project);
        return doDownloadAttachment(attachment);
    }

    public Response downloadNotebookAttachment(UUID notebookId, UUID attachmentId) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.VIEW_NOTEBOOKS);
        NotebookAttachment attachment = notebookAttachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, notebook);
        return doDownloadAttachment(attachment);
    }

    public Response downloadExperimentAttachment(UUID experimentId, UUID attachmentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        ExperimentAttachment attachment = experimentAttachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, experiment);
        return doDownloadAttachment(attachment);
    }

    private Response doDownloadAttachment(AbstractAttachment<?> attachment) {
        return Response.ok(attachment.getContent())
                .header(HttpHeaders.CONTENT_DISPOSITION, generateContentDisposition(true, attachment.getName()))
                .build();
    }

    public void deleteProjectAttachment(UUID projectId, UUID attachmentId) {
        ProjectEntity project = projectRepository.loadAndLock(projectId);
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
        ProjectAttachment attachment = projectAttachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, project);
        projectService.applyMutation(project, new ProjectMutation.DeleteProjectAttachment(attachment.getId()));
    }

    public void deleteNotebookAttachment(UUID notebookId, UUID attachmentId) {
        NotebookEntity notebook = notebookRepository.loadAndLock(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
        NotebookAttachment attachment = notebookAttachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, notebook);
        notebookService.applyMutation(notebook, new NotebookMutation.DeleteNotebookAttachment(attachment.getId()));
    }

    public void deleteExperimentAttachment(UUID experimentId, UUID attachmentId) {
        ExperimentEntity experiment = experimentRepository.loadAndLock(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
        ExperimentAttachment attachment = experimentAttachmentRepository.get(attachmentId);
        ensureCorrectParent(attachment, experiment);
        ExperimentAttachment signatureAttachment = experiment.getSignatureAttachment();
        validate(signatureAttachment == null || !signatureAttachment.getId().equals(attachment.getId()),
                "Cannot delete the attachment submitted for signature");
        experimentModelService.applyMutation(experiment, new ExperimentMutation.DeleteExperimentAttachment(attachment.getId()));
    }

    private <P extends BaseEntity> void ensureCorrectParent(AbstractAttachment<P> attachment, P expected) {
        P actualParent = attachment.getParent();
        if (actualParent != null && actualParent.getId().equals(expected.getId())) {
            return;
        }
        log.error("Attachment {} doesn't belong to requested parent entity {}", attachment, expected);
        throw new EntityNotFoundException(ELNEntityType.ATTACHMENT, attachment.getId());
    }
}
