package com.epam.indigoeln.eln.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TestSupportAPI extends BaseAPI {

    @POST
    @Path("/test-support/cleanupDatabase")
    void cleanupDatabase();
}
