package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.ExperimentAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;

import java.util.List;
import java.util.UUID;

public interface ExperimentClient extends ExperimentAPI {

    @SneakyThrows
    default List<AttachmentDTO> createExperimentAttachment(UUID experimentId, String filename, java.nio.file.Path tempDirectory, byte[] content) {
        return createExperimentAttachment(experimentId, ClientUtil.createFileUpload("file", filename, content, tempDirectory));
    }

    @POST
    @Path("/experiments/{experimentId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createExperimentAttachment(@PathParam("experimentId") UUID experimentId, ClientUtil.ClientUploadForm form);

    @GET
    @Path("/experiment/{experimentId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response downloadExperimentAttachmentClient(@PathParam("experimentId") UUID experimentId, @PathParam("attachmentId") UUID attachmentId);

    @GET
    @Path("/experiments/{experimentId}/picture")
    @Produces("image/svg+xml")
    Response getExperimentPictureClient(@PathParam("experimentId") UUID experimentId);

    @GET
    @Path("/experiments/{experimentId}/datamodel/reactions/{reactionAnchor}/picture")
    Response getReactionPictureClient(@PathParam("experimentId") UUID experimentId, @PathParam("reactionAnchor") Anchor.Reaction reactionAnchor, @Nullable @QueryParam("version") Integer version);

    @POST
    @Path("/experiments/{experimentId}/datamodel2")
    ExperimentModelPatch mutateExperimentModel2Raw(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, String mutation);
}
