package com.epam.indigoeln.signature.api;

import com.epam.indigoeln.common.model.UserRef;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jspecify.annotations.Nullable;

@Path(SignatureAdminAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SignatureAdminAPI {

    String BASE_PATH = "/api/signature/admin";

    @POST
    @Path("/migrate")
    void migrate();

    @POST
    @Path("/users")
    UserRef getOrCreateUser(@QueryParam("username") String username, @Nullable @QueryParam("firstName") String firstName, @Nullable @QueryParam("lastName") String lastName);

    @POST
    @Path("/test-support/cleanupDatabase")
    void cleanupDatabase();
}
