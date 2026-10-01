package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.ExperimentDTO;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentEditRequest;
import com.epam.indigoeln.eln.model.ExperimentRef;
import com.epam.indigoeln.eln.model.ExperimentRequest;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.model.MutationResponse;
import com.epam.indigoeln.eln.model.RevisionSummaryDTO;
import com.epam.indigoeln.eln.model.SignatureTemplateRef;
import com.epam.indigoeln.eln.quarkus.cachecontrol.Cached;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
    @Path("/notebooks/{notebookId}/experiments")
    Page<ExperimentDTO> getNotebookExperiments(@PathParam("notebookId") UUID notebookId, @QueryParam("search") @Nullable String search, @QueryParam("sort") @Nullable SortOrder sort, @QueryParam("createdByMe") @Nullable Boolean createdByMe, @QueryParam("status") @Nullable List<ExperimentStatus> statuses, @BeanParam Paging paging);

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
    @Path("/experiments/{experimentId}/attachments/{attachmentId}")
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
    MutationResponse mutateExperimentModel(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, @Nullable @QueryParam("verifyUndoRedo") Boolean verifyUndoRedo, @NotNull @Valid Mutation mutation);

    // TODO remove after frontend is updated
    @POST
    @Path("/experiments/{experimentId}/mutate4")
    MutationResponse mutateExperimentModel4(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, @NotNull @Valid Mutation mutation);

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
    @Path("/experiments/{experimentId}/workflow/submit")
    ExperimentDetailsDTO submitExperiment(@PathParam("experimentId") UUID experimentId, @QueryParam("signatureTemplateId") UUID signatureTemplateId);

    @POST
    @Path("/experiments/{experimentId}/workflow/completeAndSubmit")
    ExperimentDetailsDTO completeAndSubmitExperiment(@PathParam("experimentId") UUID experimentId, @QueryParam("signatureTemplateId") UUID signatureTemplateId);

    @POST
    @Path("/experiments/{experimentId}/print")
    Response printReport(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/experiments/{experimentId}/revisions")
    List<RevisionSummaryDTO> getExperimentRevisions(@PathParam("experimentId") UUID experimentId, @Nullable @QueryParam("flatten") Boolean flatten);

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/experiments/{experimentId}/revisions/{revisionNo}/diff")
    String getRevisionDiff(@PathParam("experimentId") UUID experimentId, @PathParam("revisionNo") @Min(2) int revisionNo);

    @GET
    @Path("/experiments/suggest")
    List<ExperimentRef> suggestExperiments(@QueryParam("search") String search);

    @GET
    @Produces("chemical/x-mdl-sdfile")
    @Path("/experiments/{experimentId}/exportSdf")
    Response exportSDF(@PathParam("experimentId") UUID experimentId);
}
