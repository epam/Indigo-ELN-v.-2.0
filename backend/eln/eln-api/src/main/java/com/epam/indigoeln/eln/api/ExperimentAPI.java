package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.quarkus.cachecontrol.Cached;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.temporal.ChronoUnit;
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
    Page<ExperimentDTO> getProjectExperiments(@PathParam("projectId") UUID projectId, @QueryParam("search") @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort, @QueryParam("createdByMe") @Nullable Boolean createdByMe, @BeanParam Paging paging);

    @GET
    @Path("/notebooks/{notebookId}/experiments")
    Page<ExperimentDTO> getNotebookExperiments(@PathParam("notebookId") UUID notebookId, @QueryParam("search") @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort, @QueryParam("createdByMe") @Nullable Boolean createdByMe, @BeanParam Paging paging);

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
    @Cached(interval = 30, unit = ChronoUnit.DAYS)
    byte[] getExperimentPicture(@PathParam("experimentId") UUID experimentId, @Nullable @QueryParam("revision") Integer revision);

    @POST
    @Path("/experiments/{experimentId}/mark")
    Boolean markExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/unmark")
    Boolean unmarkExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/access")
    List<ACLEntryDTO> updateExperimentAccess(@PathParam("experimentId") UUID experimentId, List<AccessForm> form);

    @POST
    @Path("/experiments/{experimentId}/mutate")
    ExperimentModel mutateExperimentModel(@PathParam("experimentId") UUID experimentId, MutateModelForm modelAndMutation);

    @POST
    @Path("/experiments/{experimentId}/mutate3")
    ExperimentSnapshot mutateExperimentModel3(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, Mutation mutation);

    @POST
    @Path("/experiments/{experimentId}/mutate4")
    MutationResponse mutateExperimentModel4(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, Mutation mutation);

    @POST
    @Path("/experiments/{experimentId}/datamodel2")
    JsonNode mutateExperimentModel2(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, Mutation mutation);

    @GET
    @Path("/experiments/{experimentId}/datamodel/reactions/{reactionAnchor}/picture")
    @Produces("image/svg+xml")
    @Cached(interval = 30, unit = ChronoUnit.DAYS)
    byte[] getReactionPicture(@PathParam("experimentId") UUID experimentId, @PathParam("reactionAnchor") ReactionAnchor reactionAnchor, @Nullable @QueryParam("revision") Integer revision);

    @POST
    @Path("/experiments/{experimentId}/datamodel/reactions/{reactionAnchor}/analyzeRXN")
    Map<InputAnchor, String> analyzeRXN(@PathParam("experimentId") UUID experimentId, @PathParam("reactionAnchor") ReactionAnchor reactionAnchor);

    @POST
    @Path("/experiments/{experimentId}/datamodel/reactions/{reactionAnchor}/importSDF")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    MutationResponse importSDF(@PathParam("experimentId") UUID experimentId, @PathParam("reactionAnchor") ReactionAnchor reactionAnchor, UploadForm form);

    @POST
    @Path("/experiments/{experimentId}/workflow/cancel")
    ExperimentDetailsDTO cancelExperiment(@PathParam("experimentId") UUID experimentId);

    @POST
    @Path("/experiments/{experimentId}/workflow/reopen")
    ExperimentDetailsDTO reopenExperiment(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/signatureTemplates")
    List<SignatureTemplateRef> getSignatureTemplates();

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
    @Path("/experiments/{experimentId}/print")
    Response printReport(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/experiments/{experimentId}/revisions")
    List<RevisionDetailsDTO> getExperimentRevisions(@PathParam("experimentId") UUID experimentId, @Nullable @QueryParam("editSessionId") UUID editSessionId, @Nullable @QueryParam("reverseOrder") Boolean reverseOrder);

    @GET
    @Path("/experiments/{experimentId}/revisions/summary")
    List<ExperimentRevisionSummaryDTO> getExperimentRevisionsSummary(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/experiments/{experimentId}/versions/compare")
    JsonNode compareVersions(@PathParam("experimentId") UUID experimentId, @Nullable @QueryParam("from") Integer versionFrom, @Nullable @QueryParam("to") Integer versionTo);

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/experiments/{experimentId}/versions/compare")
    String compareVersionsHTML(@PathParam("experimentId") UUID experimentId, @Nullable @QueryParam("from") Integer versionFrom, @Nullable @QueryParam("to") Integer versionTo);

    @GET
    @Path("/experiments/suggest")
    List<ExperimentRef> suggestExperiments(@QueryParam("search") String search);

    @GET
    //@Produces("chemical/x-mdl-sdfile")
    @Produces("text/plain")
    @Path("/experiments/{experimentId}/exportSdf")
    String exportSDF(@PathParam("experimentId") UUID experimentId);
}
