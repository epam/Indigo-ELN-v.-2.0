package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.UserAPI;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;

import java.util.UUID;

public interface UserClient extends UserAPI {

    @GET
    @Path("/users/{userId}/picture")
    @Produces("image/png")
    ResponseWithHeaders getUserPictureClient(@PathParam("userId") UUID userId, @QueryParam("small") @Nullable Boolean large);
}
