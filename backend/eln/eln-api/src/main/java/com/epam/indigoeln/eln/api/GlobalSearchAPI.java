package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.GlobalSearchRequest;
import com.epam.indigoeln.eln.model.GlobalSearchResultDTO;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface GlobalSearchAPI extends BaseAPI {

    @POST
    @Path("/search")
    Page<GlobalSearchResultDTO> search(GlobalSearchRequest request, @BeanParam Paging paging);
}
