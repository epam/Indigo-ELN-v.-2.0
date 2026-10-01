package com.epam.indigoeln.sampleregistration.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path(SampleRegistrationAdminAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SampleRegistrationAdminAPI {

    String BASE_PATH = "/api/sampleregistration/admin";

    @POST
    @Path("/migrate")
    void migrate();
}
