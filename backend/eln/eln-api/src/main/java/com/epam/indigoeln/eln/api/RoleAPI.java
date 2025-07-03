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
public interface RoleAPI extends BaseAPI {

    @POST
    @Path("/roles")
    RoleDTO createRole(RoleRequest request);

    @GET
    @Path("/roles")
    List<RoleDTO> getRoles();

    @GET
    @Path("/roles/suggest")
    List<RoleRef> suggestRoles();

    @PATCH
    @Path("/roles/{roleID}")
    RoleDTO updateRole(@PathParam("roleID") UUID roleID, RoleEditRequest request);

    @DELETE
    @Path("/roles/{roleID}")
    void deleteRole(@PathParam("roleID") UUID roleID);
}
