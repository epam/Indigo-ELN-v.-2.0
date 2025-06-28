package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.AttachmentMapper;
import com.epam.indigoeln.eln.model.AccessOperation;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.util.ModelUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
    AttachmentRepository attachmentRepository;
    @Inject
    AttachmentMapper attachmentMapper;

    public List<AttachmentDTO> createProjectAttachment(UUID projectId, FileUpload file) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, AccessOperation.EDIT);
        AttachmentEntity attachment = doCreateAttachment(file);
        project.getAttachments().add(attachment);
        attachment.getProjects().add(project);
        return attachmentMapper.attachmentToDTOList(project.getAttachments());
    }
    
    public List<AttachmentDTO> createNotebookAttachment(UUID notebookId, FileUpload file) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, AccessOperation.EDIT);
        AttachmentEntity attachment = doCreateAttachment(file);
        notebook.getAttachments().add(attachment);
        attachment.getNotebooks().add(notebook);
        return attachmentMapper.attachmentToDTOList(notebook.getAttachments());
    }

    public List<AttachmentDTO> createExperimentAttachment(UUID experimentId, FileUpload file) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, AccessOperation.EDIT);
        AttachmentEntity attachment = doCreateAttachment(file);
        experiment.getAttachments().add(attachment);
        attachment.getExperiments().add(experiment);
        return attachmentMapper.attachmentToDTOList(experiment.getAttachments());
    }

    private AttachmentEntity doCreateAttachment(FileUpload file) {
        byte[] content;
        try {
            content = Files.readAllBytes(file.filePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read attachment content", e);
        }
        AttachmentEntity attachment = attachmentMapper.requestToAttachment(file.fileName(), content);
        ModelUtil.updateDates(attachment, userService.getCurrentUser());
        attachmentRepository.persist(attachment);
        return attachment;
    }

    public Response downloadProjectAttachment(UUID projectId, UUID attachmentId) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, AccessOperation.VIEW);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getProjects(), project);
        return doDownloadAttachment(attachment);
    }

    public Response downloadNotebookAttachment(UUID notebookId, UUID attachmentId) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, AccessOperation.VIEW);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getNotebooks(), notebook);
        return doDownloadAttachment(attachment);
    }

    public Response downloadExperimentAttachment(UUID experimentId, UUID attachmentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, AccessOperation.VIEW);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getExperiments(), experiment);
        return doDownloadAttachment(attachment);
    }

    public Response doDownloadAttachment(AttachmentEntity attachment) {
        return Response.ok(attachment.getContent())
                .header("Content-Disposition", "attachment; filename=" + attachment.getName()).build();
    }

    public void deleteProjectAttachment(UUID projectId, UUID attachmentId) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, AccessOperation.EDIT);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getProjects(), project);
        doDeleteAttachment(project, attachment.getProjects(), attachment);
    }

    public void deleteNotebookAttachment(UUID notebookId, UUID attachmentId) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, AccessOperation.EDIT);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getNotebooks(), notebook);
        doDeleteAttachment(notebook, attachment.getNotebooks(), attachment);
    }

    public void deleteExperimentAttachment(UUID experimentId, UUID attachmentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, AccessOperation.EDIT);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getExperiments(), experiment);
        doDeleteAttachment(experiment, attachment.getExperiments(), attachment);
    }

    public <E extends BaseEntity & WithAttachments> void doDeleteAttachment(E parent, Collection<E> parents, AttachmentEntity attachment) {
        parent.getAttachments().remove(attachment);
        parents.remove(parent);
        attachmentRepository.delete(attachment);
    }

    private <E extends BaseEntity> void ensureCorrectParent(AttachmentEntity attachment, Collection<E> parents, E expected) {
        for (E parent : parents) {
            if (parent.getId().equals(expected.getId())) {
                return;
            }
        }
        log.error("Attachment {} doesn't belong to requested parent entity {}", attachment, expected);
        throw new EntityNotFoundException(EntityType.ATTACHMENT, attachment.getId());
    }
}
