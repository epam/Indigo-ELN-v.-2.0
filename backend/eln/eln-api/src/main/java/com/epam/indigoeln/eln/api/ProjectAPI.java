package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.annotation.Nullable;
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
    Page<ProjectDTO> getProjects(@QueryParam("search") @Nullable String search, @BeanParam Paging paging);

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

    @GET
    @Path("/project/{projectId}/attachments/{attachmentId}")
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
    @Path("/projects/keywords/suggest")
    List<String> suggestProjectKeywords(@Nullable @QueryParam("search") String search, @BeanParam Paging paging);
}
