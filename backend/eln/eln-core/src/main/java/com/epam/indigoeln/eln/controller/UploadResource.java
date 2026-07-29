package com.epam.indigoeln.eln.controller;

import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.UploadAPI;
import com.epam.indigoeln.eln.service.UploadService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(BaseAPI.BASE_PATH)
public class UploadResource implements UploadAPI {

    @Inject
    UploadService uploadService;

    @Override
    public Response uploadFileContent(String path, UploadForm form) {
        uploadService.uploadAttachment("attachment/" + path, form.getFile());
        return Response.ok().build();
    }
}
