package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.UserAPI;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

public interface UserClient extends UserAPI {

    @GET
    @Path("/users/{userId}/picture")
    @Produces("image/png")
    Response getUserPictureClient(@PathParam("userId") UUID userId, @QueryParam("small") @Nullable Boolean large);
}
