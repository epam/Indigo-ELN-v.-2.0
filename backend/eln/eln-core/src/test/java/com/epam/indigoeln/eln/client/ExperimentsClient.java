package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.ExperimentsAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface ExperimentsClient extends ExperimentsAPI {

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
    ResponseWithHeaders downloadExperimentAttachmentClient(@PathParam("experimentId") UUID experimentId, @PathParam("attachmentId") UUID attachmentId);
}
