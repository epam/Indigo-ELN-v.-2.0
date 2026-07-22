package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.eln.model.*;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProjectAPI extends BaseAPI {

    @POST
    @Path("/projects")
    ProjectDetailsDTO createProject(ProjectRequest request);

    @GET
    @Path("/projects")
    Page<ProjectDTO> getProjects(@QueryParam("search") @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort, @QueryParam("createdByMe") @Nullable Boolean createdByMe, @BeanParam Paging paging);

    @GET
    @Path("/projects/existence")
    ProjectExistenceCheckDTO checkProjectNameExistence(
            @QueryParam("name") @NotEmpty String name);

    @GET
    @Path("/projects/{projectId}")
    ProjectDetailsDTO getProject(@PathParam("projectId") UUID projectId);

    @PATCH
    @Path("/projects/{projectId}")
    ProjectDetailsDTO editProject(@PathParam("projectId") UUID projectId, ProjectEditRequest request);

    @POST
    @Path("/projects/{projectId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createProjectAttachment(@PathParam("projectId") UUID projectId, UploadForm form);

    @POST
    @Path("/projects/{projectId}/attachments/complete")
    List<AttachmentDTO> completeProjectAttachment(@PathParam("projectId") UUID projectId);

    @GET
    @Path("/projects/{projectId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response downloadProjectAttachment(@PathParam("projectId") UUID projectId, @PathParam("attachmentId") UUID attachmentId);

    @DELETE
    @Path("/projects/{projectId}/attachments/{attachmentId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void deleteProjectAttachment(@PathParam("projectId") UUID projectId, @PathParam("attachmentId") UUID attachmentId);

    @POST
    @Path("/projects/{projectId}/access")
    List<ACLEntryDTO> updateProjectAccess(@PathParam("projectId") UUID projectId, List<AccessForm> form);

    @GET
    @Path("/projects/{projectId}/nestedAccess")
    List<NestedACLEntryDTO> getNestedProjectAccess(@PathParam("projectId") UUID projectId);

    @GET
    @Path("/projects/{projectId}/revisions")
    List<RevisionSummaryDTO> getProjectRevisions(@PathParam("projectId") UUID projectId);
}
