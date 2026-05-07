package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface NotebookAPI extends BaseAPI {

    @POST
    @Path("/projects/{projectId}/notebooks")
    NotebookDetailsDTO createNotebook(@PathParam("projectId") UUID projectId, NotebookRequest request);

    @GET
    @Path("/notebooks/{notebookId}")
    NotebookDetailsDTO getNotebook(@PathParam("notebookId") UUID notebookId);

    @GET
    @Path("/projects/{projectId}/notebooks")
    Page<NotebookDTO> getProjectNotebooks(@PathParam("projectId") UUID projectId, @QueryParam("search") @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort,
                                          @QueryParam("createdByMe") @Nullable Boolean createdByMe, @BeanParam Paging paging);

    @GET
    @Path("/notebooks/existence")
    NotebookExistenceCheckDTO checkNotebookNameExistence(
            @QueryParam("name") @NotEmpty String name);

    @PATCH
    @Path("/notebooks/{notebookId}")
    NotebookDetailsDTO editNotebook(@PathParam("notebookId") UUID notebookId, NotebookEditRequest request);

    @POST
    @Path("/notebooks/{notebookId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createNotebookAttachment(@PathParam("notebookId") UUID notebookId, com.epam.indigoeln.common.model.UploadForm form);

    @GET
    @Path("/notebook/{notebookId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response downloadNotebookAttachment(@PathParam("notebookId") UUID notebookId, @PathParam("attachmentId") UUID attachmentId);

    @DELETE
    @Path("/notebooks/{notebookId}/attachments/{attachmentId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void deleteNotebookAttachment(@PathParam("notebookId") UUID notebookId, @PathParam("attachmentId") UUID attachmentId);

    @POST
    @Path("/notebooks/{notebookId}/access")
    List<ACLDetailsEntryDTO> updateNotebookAccess(@PathParam("notebookId") UUID notebookId, List<AccessForm> form);

    @GET
    @Path("/notebooks/{notebookId}/nestedAccess")
    List<NestedACLEntryDTO> getNestedNotebookAccess(@PathParam("notebookId") UUID notebookId);

    @GET
    @Path("/notebooks/{notebookId}/revisions")
    List<RevisionDetailsDTO> getNotebookRevisions(@PathParam("notebookId") UUID notebookId);
}
