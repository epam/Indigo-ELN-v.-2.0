package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.ProjectAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.test.ClientUtil;
import com.epam.indigoeln.test.ClientUtil.ClientUploadForm;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.util.List;
import java.util.UUID;

public interface ProjectClient extends ProjectAPI {

    @SneakyThrows
    default String createProjectAttachment(UUID projectId, String filename, byte[] content) {
        return createProjectAttachmentClient(projectId, ClientUtil.createFileUpload(filename, content));
    }

    @POST
    @Path("/projects/{projectId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    String createProjectAttachmentClient(@PathParam("projectId") UUID projectId, ClientUploadForm form);
}
