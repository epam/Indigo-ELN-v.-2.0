package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.UploadAPI;
import com.epam.indigoeln.test.ClientUtil;
import com.epam.indigoeln.test.ClientUtil.ClientUploadForm;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;

public interface UploadClient extends UploadAPI {

    @SneakyThrows
    default Response uploadFileContent(String stringPath, String filename, byte[] content) {
        return uploadFileContent(stringPath, ClientUtil.createFileUpload(filename, content));
    }

    @POST
    @Path("/upload/{path}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadFileContent(@PathParam("path") String path, ClientUploadForm form);
}
