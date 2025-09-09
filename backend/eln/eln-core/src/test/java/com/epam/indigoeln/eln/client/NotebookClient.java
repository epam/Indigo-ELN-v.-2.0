package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.NotebookAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.test.ResponseWithHeaders;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface NotebookClient extends NotebookAPI {

    @SneakyThrows
    default List<AttachmentDTO> createNotebookAttachment(UUID notebookId, String filename, Path tempDirectory, byte[] content) {
        return createNotebookAttachment(notebookId, ClientUtil.createFileUpload("file", filename, content, tempDirectory));
    }

    @POST
    @jakarta.ws.rs.Path("/notebooks/{notebookId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createNotebookAttachment(@PathParam("notebookId") UUID notebookId, ClientUtil.ClientUploadForm form);

    @GET
    @jakarta.ws.rs.Path("/notebook/{notebookId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    ResponseWithHeaders downloadNotebookAttachmentClient(@PathParam("notebookId") UUID notebookId, @PathParam("attachmentId") UUID attachmentId);
}
