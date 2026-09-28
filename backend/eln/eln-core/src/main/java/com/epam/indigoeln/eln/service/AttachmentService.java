package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.util.Pair;
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
import java.util.*;

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
    AttachmentRepository attachmentRepository;
    @Inject
    AttachmentMapper attachmentMapper;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    ProjectService projectService;
    @Inject
    NotebookService notebookService;
    @Inject
    ProjectAttachmentRepository projectAttachmentRepository;
    @Inject
    NotebookAttachmentRepository notebookAttachmentRepository;
    @Inject
    ExperimentAttachmentRepository experimentAttachmentRepository;

    public Map<String, String> prepareProjectAttachment(UUID projectId, String filename, long size, boolean useMutation) {
        ProjectEntity project = projectRepository.get(projectId);
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
        ProjectAttachment attachment = new ProjectAttachment();
        setAttachmentFields(attachment, filename, size);
        String presignedUrl = projectAttachmentRepository.persistAndCreatePresignedUrl(attachment);

        if (useMutation) {
            projectService.applyMutation(project, new ProjectMutation.CreateProjectAttachment(attachment.getId()));
        } else {
            doAddProjectAttachment(project, attachment);
        }

        return Map.of(
                "url", presignedUrl,
                "id", attachment.getId().toString()
        );
    }

    public Map<String, String> prepareNotebookAttachment(UUID notebookId, String filename, long size, boolean useMutation) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
        NotebookAttachment attachment = new NotebookAttachment();
        setAttachmentFields(attachment, filename, size);
        String presignedUrl = notebookAttachmentRepository.persistAndCreatePresignedUrl(attachment);

        if (useMutation) {
            notebookService.applyMutation(notebook, new NotebookMutation.CreateNotebookAttachment(attachment.getId()));
        } else {
            doAddNotebookAttachment(notebook, attachment);
        }

        return Map.of(
                "url", presignedUrl,
                "id", attachment.getId().toString()
        );
    }

    public Map<String, String> prepareExperimentAttachment(UUID experimentId, String filename, long size, @Nullable Boolean useMutation) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
        ExperimentAttachment attachment = new ExperimentAttachment();
        setAttachmentFields(attachment, filename, size);
        String presignedUrl = experimentAttachmentRepository.persistAndCreatePresignedUrl(attachment);

        if (useMutation == Boolean.TRUE) {
            experimentModelService.applyMutation(experiment, new ExperimentMutation.CreateExperimentAttachment(attachment.getId()));
        } else if (useMutation == Boolean.FALSE) {
            doAddExperimentAttachment(experiment, attachment);
        }

        return Map.of(
                "url", presignedUrl,
                "id", attachment.getId().toString()
        );
    }

    private void setAttachmentFields(AbstractAttachment<?> attachment, String filename, long size) {
        attachment.setName(filename);
        attachment.setSize(size);
        attachment.setDeleted(false);
        updateDates(attachment, userService.getCurrentUserEntity());
    }

    public List<AttachmentDTO> completeExperimentAttachment(UUID experimentId, UUID attachmentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        attachmentRepository.complete(attachmentId);
        return attachmentMapper.attachmentToDTOList(experiment.getAttachments());
    }

    public List<AttachmentDTO> completeProjectAttachment(UUID projectId, UUID attachmentId) {
        ProjectEntity project = projectRepository.get(projectId);
        attachmentRepository.complete(attachmentId);
        return attachmentMapper.attachmentToDTOList(project.getAttachments());
    }

    public List<AttachmentDTO> completeNotebookAttachment(UUID notebookId, UUID attachmentId) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        attachmentRepository.complete(attachmentId);
        return attachmentMapper.attachmentToDTOList(notebook.getAttachments());
    }

    public void doAddProjectAttachment(ProjectEntity entity, ProjectAttachment attachment) {
        entity.getAttachments().add(attachment);
        attachment.setParent(entity);
    }

    public void doAddNotebookAttachment(NotebookEntity entity, NotebookAttachment attachment) {
        entity.getAttachments().add(attachment);
        attachment.setParent(entity);
    }

    public void doAddExperimentAttachment(ExperimentEntity entity, ExperimentAttachment attachment) {
        entity.getAttachments().add(attachment);
        attachment.setParent(entity);
    }

    private byte[] readFile(FileUpload file) {
        try {
            return Files.readAllBytes(file.filePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read attachment content", e);
        }
    }

    private Pair<AttachmentEntity, String> doCreateAttachment(String filename, long size) {
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setName(filename);
        attachment.setSize(size);
        attachment.setDeleted(false);

        updateDates(attachment, userService.getCurrentUserEntity());
        String presignedUrl = attachmentRepository.persistAndCreatePresignedUrl(attachment);
        return new Pair<>(attachment, presignedUrl);
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
                .header(HttpHeaders.CONTENT_DISPOSITION, generateContentDisposition(true, attachment.getName()))
                .build();
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
        ExperimentEntity experiment = experimentRepository.loadAndLock(experimentId);
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
        throw new EntityNotFoundException(ELNEntityType.ATTACHMENT, attachment.getId());
    }
}
