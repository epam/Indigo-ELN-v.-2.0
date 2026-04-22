package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.NotebookAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.util.List;
import java.util.UUID;

public interface NotebookClient extends NotebookAPI {

    @SneakyThrows
    default List<AttachmentDTO> createNotebookAttachment(UUID notebookId, String filename, java.nio.file.Path tempDirectory, byte[] content) {
        return createNotebookAttachment(notebookId, ClientUtil.createFileUpload("file", filename, content, tempDirectory));
    }

    @POST
    @Path("/notebooks/{notebookId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    List<AttachmentDTO> createNotebookAttachment(@PathParam("notebookId") UUID notebookId, ClientUtil.ClientUploadForm form);
}
