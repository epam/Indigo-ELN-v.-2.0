package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.UploadForm;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UploadAPI extends BaseAPI {
    @POST
    @Path("/upload/{path}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadFileContent(@PathParam("path") String path, UploadForm form);
}
