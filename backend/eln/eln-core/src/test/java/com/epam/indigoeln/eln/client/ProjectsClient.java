package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.ProjectsAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface ProjectsClient extends ProjectsAPI {

    @SneakyThrows
    default List<AttachmentDTO> createProjectAttachment(UUID projectId, String filename, Path tempDirectory, byte[] content) {
        return createProjectAttachmentClient(projectId, ClientUtil.createFileUpload("file", filename, content, tempDirectory));
    }

    @POST
    @jakarta.ws.rs.Path("/projects/{projectId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createProjectAttachmentClient(@PathParam("projectId") UUID projectId, ClientUtil.ClientUploadForm form);

    @GET
    @jakarta.ws.rs.Path("/project/{projectId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    ResponseWithHeaders downloadProjectAttachmentClient(@PathParam("projectId") UUID projectId, @PathParam("attachmentId") UUID attachmentId);
}
