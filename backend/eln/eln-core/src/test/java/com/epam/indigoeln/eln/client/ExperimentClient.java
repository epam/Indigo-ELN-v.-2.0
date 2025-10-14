package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.ExperimentAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.model.Anchor;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface ExperimentClient extends ExperimentAPI {

    @SneakyThrows
    default List<AttachmentDTO> createExperimentAttachment(UUID experimentId, String filename, Path tempDirectory, byte[] content) {
        return createExperimentAttachment(experimentId, ClientUtil.createFileUpload("file", filename, content, tempDirectory));
    }

    @POST
    @jakarta.ws.rs.Path("/experiments/{experimentId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createExperimentAttachment(@PathParam("experimentId") UUID experimentId, ClientUtil.ClientUploadForm form);

    @GET
    @jakarta.ws.rs.Path("/experiment/{experimentId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response downloadExperimentAttachmentClient(@PathParam("experimentId") UUID experimentId, @PathParam("attachmentId") UUID attachmentId);

    @GET
    @jakarta.ws.rs.Path("/experiments/{experimentId}/picture")
    @Produces("image/svg+xml")
    Response getExperimentPictureClient(@PathParam("experimentId") UUID experimentId);

    @GET
    @jakarta.ws.rs.Path("/experiments/{experimentId}/datamodel/reactions/{reactionAnchor}/picture")
    Response getReactionPictureClient(@PathParam("experimentId") UUID experimentId, @PathParam("reactionAnchor") Anchor.Reaction reactionAnchor, @Nullable @QueryParam("version") Integer version);
}
