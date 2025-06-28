package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ExperimentsAPI extends BaseAPI {

    @POST
    @Path("/notebooks/{notebookId}/experiments")
    ExperimentDetailsDTO createExperiment(@PathParam("notebookId") UUID notebookId, ExperimentRequest request);

    @GET
    @Path("/experiments/{experimentId}")
    ExperimentDetailsDTO getExperiment(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/projects/{projectId}/experiments")
    Page<ExperimentDTO> getProjectExperiments(@PathParam("projectId") UUID projectId, @BeanParam Paging paging);

    @GET
    @Path("/notebooks/{notebookId}/experiments")
    Page<ExperimentDTO> getNotebookExperiments(@PathParam("notebookId") UUID notebookId, @BeanParam Paging paging);

    @PATCH
    @Path("/experiments/{experimentId}")
    ExperimentDetailsDTO editExperiment(@PathParam("experimentId") UUID experimentId, ExperimentEditRequest request);

    @POST
    @Path("/experiments/{experimentId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createExperimentAttachment(@PathParam("experimentId") UUID experimentId, UploadForm form);

    @GET
    @Path("/experiment/{experimentId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response downloadExperimentAttachment(@PathParam("experimentId") UUID experimentId, @PathParam("attachmentId") UUID attachmentId);

    @DELETE
    @Path("/experiments/{experimentId}/attachments/{attachmentId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void deleteExperimentAttachment(@PathParam("experimentId") UUID experimentId, @PathParam("attachmentId") UUID attachmentId);

    @POST
    @Path("/experiments/{experimentId}/mark")
    Boolean markExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/unmark")
    Boolean unmarkExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/access")
    List<ACLEntryDTO> updateExperimentAccess(@PathParam("experimentId") UUID experimentId, List<AccessForm> form);

    @GET
    @Path("/experiments/{experimentId}/datamodel")
    ExperimentModel getExperimentModel(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/datamodel")
    ExperimentModel mutateExperimentModel(@PathParam("experimentId") UUID experimentId, MutateModelForm modelAndMutation);
}
