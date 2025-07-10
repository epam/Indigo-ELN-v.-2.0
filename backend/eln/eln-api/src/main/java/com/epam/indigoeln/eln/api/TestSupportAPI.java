package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.TotalCounts;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TestSupportAPI extends BaseAPI {

    @POST
    @Path("/test-support/cleanupDatabase")
    void cleanupDatabase();
}
