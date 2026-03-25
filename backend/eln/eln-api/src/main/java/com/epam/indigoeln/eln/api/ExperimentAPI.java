package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ExperimentAPI extends BaseAPI {

    @POST
    @Path("/notebooks/{notebookId}/experiments")
    ExperimentDetailsDTO createExperiment(@PathParam("notebookId") UUID notebookId, ExperimentRequest request);

    @GET
    @Path("/experiments/{experimentId}")
    ExperimentDetailsDTO getExperiment(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/experiments/{experimentId}/snapshot")
    ExperimentSnapshot getExperimentSnapshot(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/projects/{projectId}/experiments")
    Page<ExperimentDTO> getProjectExperiments(@PathParam("projectId") UUID projectId, @QueryParam("sort") @Nullable SortOrder sort,
                                              @QueryParam("createdByMe") @Nullable Boolean createdByMe, @BeanParam Paging paging);

    @GET
    @Path("/notebooks/{notebookId}/experiments")
    Page<ExperimentDTO> getNotebookExperiments(@PathParam("notebookId") UUID notebookId, @QueryParam("sort") @Nullable SortOrder sort,
                                               @QueryParam("createdByMe") @Nullable Boolean createdByMe, @BeanParam Paging paging);

    @GET
    @Path("/experiments/marked")
    List<ExperimentDTO> getMarkedExperiments();

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

    @GET
    @Path("/experiments/{experimentId}/picture")
    @Produces("image/svg+xml")
    byte[] getExperimentPicture(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/mark")
    Boolean markExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/unmark")
    Boolean unmarkExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/access")
    List<ACLDetailsEntryDTO> updateExperimentAccess(@PathParam("experimentId") UUID experimentId, List<AccessForm> form);

    @POST
    @Path("/experiments/{experimentId}/mutate")
    ExperimentModel mutateExperimentModel(@PathParam("experimentId") UUID experimentId, MutateModelForm modelAndMutation);

    @POST
    @Path("/experiments/{experimentId}/mutate3")
    ExperimentSnapshot mutateExperimentModel3(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, Mutation mutation);

    @POST
    @Path("/experiments/{experimentId}/datamodel2")
    ExperimentPatch mutateExperimentModel2(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, Mutation mutation);

    @GET
    @Path("/experiments/{experimentId}/datamodel/reactions/{reactionAnchor}/picture")
    Response getReactionPicture(@PathParam("experimentId") UUID experimentId, @PathParam("reactionAnchor") ReactionAnchor reactionAnchor, @Nullable @QueryParam("version") Integer version);

    @POST
    @Path("/experiments/{experimentId}/datamodel/reactions/{reactionAnchor}/analyzeRXN")
    Map<InputAnchor, @org.jspecify.annotations.Nullable FindSamplesRequest> analyzeRXN(@PathParam("experimentId") UUID experimentId, @PathParam("reactionAnchor") ReactionAnchor reactionAnchor);

    @POST
    @Path("/experiments/{experimentId}/workflow/cancel")
    ExperimentDetailsDTO cancelExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/workflow/reopen")
    ExperimentDetailsDTO reopenExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/workflow/complete")
    ExperimentDetailsDTO completeExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/workflow/sign")
    ExperimentDetailsDTO submitExperiment(@PathParam("experimentId") UUID experimentId, @QueryParam("signatureTemplateId") UUID signatureTemplateId);

    @POST
    @Path("/experiments/{experimentId}/workflow/completeAndSubmit")
    ExperimentDetailsDTO completeAndSubmitExperiment(@PathParam("experimentId") UUID experimentId, @QueryParam("signatureTemplateId") UUID signatureTemplateId);

    @POST
    @Path("/experiments/{experimentId}/workflow/approve")
    ExperimentForSignatureDTO approveExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/workflow/reject")
    ExperimentForSignatureDTO rejectExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/workflow/resubmit")
    ExperimentDetailsDTO resubmitExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/print")
    Response printReport(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/experiments/{experimentId}/revisions")
    List<RevisionDetailsDTO<ExperimentPatch>> getExperimentRevisions(@PathParam("experimentId") UUID experimentId, @Nullable @QueryParam("editSessionId") UUID editSessionId, @Nullable @QueryParam("reverseOrder") Boolean reverseOrder);

    @GET
    @Path("/experiments/{experimentId}/revisions/summary")
    List<ExperimentRevisionSummaryDTO> getExperimentRevisionsSummary(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/experiments/{experimentId}/versions/compare")
    ExperimentPatch compareVersions(@PathParam("experimentId") UUID experimentId, @Nullable @QueryParam("from") Integer versionFrom, @Nullable @QueryParam("to") Integer versionTo);

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/experiments/{experimentId}/versions/compare")
    String compareVersionsHTML(@PathParam("experimentId") UUID experimentId, @Nullable @QueryParam("from") Integer versionFrom, @Nullable @QueryParam("to") Integer versionTo);

    @GET
    @Path("/experiments/suggest")
    List<ExperimentRef> suggestExperiments(@QueryParam("search") String search);
}
