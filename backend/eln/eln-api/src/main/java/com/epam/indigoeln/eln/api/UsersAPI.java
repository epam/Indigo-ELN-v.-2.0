package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UsersAPI extends BaseAPI {

    @POST
    @Path("/users")
    UserDetailsDTO createUser(UserRequest request);

    @GET
    @Path("/users")
    Page<UserDTO> getUsers(@QueryParam("search") @Nullable String search, @BeanParam Paging paging);

    @GET
    @Path("/users/{userId}")
    UserDetailsDTO getUser(@PathParam("userId") UUID userId);

    @POST
    @Path("/users/{userId}/access")
    List<ACLEntryDTO> updateUserAccess(@PathParam("userId") UUID userId, List<AccessForm> form);

    @GET
    @Path("/users/suggest")
    List<UserRef> suggestUsers(@QueryParam("search") @Nullable String search, @BeanParam Paging paging);

}
