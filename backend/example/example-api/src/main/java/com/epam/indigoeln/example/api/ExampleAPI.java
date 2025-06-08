package com.epam.indigoeln.example.api;

import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path(ExampleAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ExampleAPI {

    String BASE_PATH = "/api/example";

    // TODO add file upload form
    // TODO add methods with failed parameter and return value validations

    @GET
    @Path("/info")
    Map<String, String> getInfo();

    @POST
    @Path(("/sum"))
    MinMax sum(@QueryParam("a") int a, @QueryParam("b") int b);

    record MinMax (
            @Positive int min,
            @Positive int max
    ) {}
}
