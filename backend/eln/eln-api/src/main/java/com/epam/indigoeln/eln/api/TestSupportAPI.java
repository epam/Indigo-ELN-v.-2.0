package com.epam.indigoeln.eln.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TestSupportAPI extends BaseAPI {

    @GET
    @Path("/testsupport/storage/list")
    List<String> storageList(@QueryParam("path") String path);

    @GET
    @Path("/testsupport/storage/read")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response storageRead(@QueryParam("path") String path);
}
