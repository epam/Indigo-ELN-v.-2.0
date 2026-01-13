package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.NotebookAPI;
import com.epam.indigoeln.eln.api.UploadForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.NotebookService;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class NotebookResource implements NotebookAPI {

    @Inject
    NotebookService notebookService;
    @Inject
    AttachmentService attachmentService;

    @Override
    public @NotNull @Valid NotebookDetailsDTO createNotebook(@NotNull UUID projectId, @NotNull @Valid NotebookRequest request) {
        return notebookService.createNotebook(projectId, request);
    }

    @Override
    public @NotNull @Valid NotebookDetailsDTO getNotebook(@NotNull UUID notebookId) {
        return notebookService.getNotebook(notebookId);
    }

    @Override
    public @NotNull @Valid Page<NotebookDTO> getProjectNotebooks(@NotNull UUID projectId, @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort,
                                                                 @QueryParam("createdByMe") @Nullable Boolean createdByMe, @Valid Paging paging) {
        return notebookService.getNotebooks(projectId, search, sort, createdByMe, paging);
    }

    @Override
    public @NotNull @Valid NotebookDetailsDTO editNotebook(@NotNull UUID notebookId, @NotNull @Valid NotebookEditRequest request) {
        return notebookService.editNotebook(notebookId, request);
    }

    @Override
    public List<AttachmentDTO> createNotebookAttachment(UUID notebookId, UploadForm form) {
        return attachmentService.createNotebookAttachment(notebookId, form.getFile());
    }

    @Override
    public Response downloadNotebookAttachment(UUID notebookId, UUID attachmentId) {
        return attachmentService.downloadNotebookAttachment(notebookId, attachmentId);
    }

    @Override
    public void deleteNotebookAttachment(UUID notebookId, UUID attachmentId) {
        attachmentService.deleteNotebookAttachment(notebookId, attachmentId);
    }

    @Override
    public List<ACLDetailsEntryDTO> updateNotebookAccess(@NotNull UUID notebookId, @NotNull @Valid List<AccessForm> form) {
        return notebookService.updateNotebookAccess(notebookId, form);
    }

    @Override
    public List<NestedACLEntryDTO> getNestedNotebookAccess(UUID notebookId) {
        return notebookService.getNestedNotebookAccess(notebookId);
    }

    @Override
    public List<RevisionDetailsDTO<NotebookPatch>> getNotebookRevisions(UUID notebookId) {
        return notebookService.getNotebookRevisions(notebookId);
    }
}
