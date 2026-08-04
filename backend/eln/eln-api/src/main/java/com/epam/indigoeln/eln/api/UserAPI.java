package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.model.CurrentUserDTO;
import com.epam.indigoeln.eln.model.UserDTO;
import com.epam.indigoeln.eln.model.UserRequest;
import com.epam.indigoeln.eln.quarkus.cachecontrol.Cached;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.temporal.ChronoUnit;
import java.util.List;

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
    Page<UserDTO> getUsers(@QueryParam("search") @Nullable String search, @BeanParam Paging paging);

    @GET
    @Path("/users/{username}")
    UserDTO getUser(@PathParam("username") String username);

    @GET
    @Path("/users/{username}/picture")
    @Cached(interval = 1, unit = ChronoUnit.DAYS)
    @Produces("image/png")
    byte[] getUserPicture(@PathParam("username") String username, @QueryParam("small") @Nullable Boolean large);

    @GET
    @Path("/users/suggest")
    List<UserRef> suggestUsers(@QueryParam("search") @Nullable String search);
}
