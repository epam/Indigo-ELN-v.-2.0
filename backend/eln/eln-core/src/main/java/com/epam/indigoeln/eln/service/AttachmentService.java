package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.AttachmentMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.repository.AttachmentRepository;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
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
    AttachmentRepository attachmentRepository;
    @Inject
    AttachmentMapper attachmentMapper;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    ProjectService projectService;
    @Inject
    NotebookService notebookService;

    public List<AttachmentDTO> createProjectAttachment(UUID projectId, FileUpload file) {
        return createProjectAttachment(projectId, file.fileName(), readFile(file));
    }

    public List<AttachmentDTO> createProjectAttachment(UUID projectId, String filename, byte[] content) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
        AttachmentEntity attachment = doCreateAttachment(filename, content);
        projectService.applyMutation(project, new ProjectMutation.CreateProjectAttachment(attachment.getId()));
        return attachmentMapper.attachmentToDTOList(project.getAttachments());
    }

    public List<AttachmentDTO> createNotebookAttachment(UUID notebookId, FileUpload file) {
        return createNotebookAttachment(notebookId, file.fileName(), readFile(file));
    }

    public List<AttachmentDTO> createNotebookAttachment(UUID notebookId, String filename, byte[] content) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
        AttachmentEntity attachment = doCreateAttachment(filename, content);
        notebookService.applyMutation(notebook, new NotebookMutation.CreateNotebookAttachment(attachment.getId()));
        return attachmentMapper.attachmentToDTOList(notebook.getAttachments());
    }

    public List<AttachmentDTO> createExperimentAttachment(UUID experimentId, FileUpload file) {
        return createExperimentAttachment(experimentId, file.fileName(), readFile(file));
    }

    public List<AttachmentDTO> createExperimentAttachment(UUID experimentId, String filename, byte[] content) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        createExperimentAttachment(experiment, filename, content);
        return attachmentMapper.attachmentToDTOList(experiment.getAttachments());
    }

    public AttachmentEntity createExperimentAttachment(ExperimentEntity experiment, String filename, byte[] content) {
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
        AttachmentEntity attachment = doCreateAttachment(filename, content);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.CreateExperimentAttachment(attachment.getId()));
        return attachment;
    }

    private byte[] readFile(FileUpload file) {
        try {
            return Files.readAllBytes(file.filePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read attachment content", e);
        }
    }

    private AttachmentEntity doCreateAttachment(String filename, byte[] content) {
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setName(filename);
        attachment.setSize((long) content.length);
        attachment.setDeleted(false);
        attachment.setContent(content);

        updateDates(attachment, userService.getCurrentUserEntity());
        attachmentRepository.persist(attachment);
        return attachment;
    }

    public Response downloadProjectAttachment(UUID projectId, UUID attachmentId) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.VIEW_PROJECTS);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getProjects(), project);
        return doDownloadAttachment(attachment);
    }

    public Response downloadNotebookAttachment(UUID notebookId, UUID attachmentId) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.VIEW_NOTEBOOKS);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getNotebooks(), notebook);
        return doDownloadAttachment(attachment);
    }

    public Response downloadExperimentAttachment(UUID experimentId, UUID attachmentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getExperiments(), experiment);
        return doDownloadAttachment(attachment);
    }

    private Response doDownloadAttachment(AttachmentEntity attachment) {
        return Response.ok(attachment.getContent())
                .header("Content-Disposition", "attachment; filename=" + attachment.getName()).build();
    }

    public void deleteProjectAttachment(UUID projectId, UUID attachmentId) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getProjects(), project);
        projectService.applyMutation(project, new ProjectMutation.DeleteProjectAttachment(attachment.getId()));
    }

    public void deleteNotebookAttachment(UUID notebookId, UUID attachmentId) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
        AttachmentEntity attachment = attachmentRepository.load(attachmentId);
        ensureCorrectParent(attachment, attachment.getNotebooks(), notebook);
        notebookService.applyMutation(notebook, new NotebookMutation.DeleteNotebookAttachment(attachment.getId()));
    }

    public void deleteExperimentAttachment(UUID experimentId, UUID attachmentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
        AttachmentEntity attachment = attachmentRepository.get(attachmentId);
        ensureCorrectParent(attachment, attachment.getExperiments(), experiment);
        experimentModelService.applyMutation(experiment, new ExperimentMutation.DeleteExperimentAttachment(attachment.getId()));
    }

    public <E extends BaseEntity & WithAttachments> void doDeleteAttachment(E parent, Collection<E> parents, AttachmentEntity attachment) {
        parent.getAttachments().remove(attachment);
        parents.remove(parent);
        attachment.setDeleted(false);
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
