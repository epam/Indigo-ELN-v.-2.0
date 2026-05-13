package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.ExperimentAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.util.List;
import java.util.UUID;

public interface ExperimentClient extends ExperimentAPI {

    @SneakyThrows
    default List<AttachmentDTO> createExperimentAttachment(UUID experimentId, String filename, java.nio.file.Path tempDirectory, byte[] content) {
        return createExperimentAttachment(experimentId, com.epam.indigoeln.test.ClientUtil.createFileUpload("file", filename, content, tempDirectory));
    }

    @POST
    @Path("/experiments/{experimentId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createExperimentAttachment(@PathParam("experimentId") UUID experimentId, com.epam.indigoeln.test.ClientUtil.ClientUploadForm form);

    @POST
    @Path("/experiments/{experimentId}/datamodel2")
    JsonNode mutateExperimentModel2Raw(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, String mutation);
}
