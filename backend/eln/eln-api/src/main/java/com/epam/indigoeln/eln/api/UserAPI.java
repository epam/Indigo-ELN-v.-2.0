package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UserAPI extends BaseAPI {

    @GET
    @Path("/currentUser")
    CurrentUserDTO getCurrentUser();

    @POST
    @Path("/users")
    UserDTO createUser(UserRequest request);

    @GET
    @Path("/users")
    Page<UserDTO> getUsers(@QueryParam("search") @Nullable String search, @QueryParam("username") @Nullable String username, @BeanParam Paging paging);

    @GET
    @Path("/users/{userId}")
    UserDTO getUser(@PathParam("userId") UUID userId);

    @GET
    @Path("/users/{userId}/picture")
    @com.epam.indigoeln.eln.quarkus.cachecontrol.Cached(interval = 1, unit = ChronoUnit.DAYS)
    @Produces("image/png")
    byte[] getUserPicture(@PathParam("userId") UUID userId, @QueryParam("small") @Nullable Boolean large);

    @GET
    @Path("/users/suggest")
    List<UserRef> suggestUsers(@QueryParam("search") @Nullable String search);
}
