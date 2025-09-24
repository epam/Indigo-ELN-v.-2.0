package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface GlobalSearchAPI extends BaseAPI {

    @POST
    @Path("/search")
    Page<GlobalSearchResultDTO> search(GlobalSearchRequest request, @BeanParam Paging paging);
}
