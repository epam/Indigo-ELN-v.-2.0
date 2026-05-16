package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.eln.model.GlobalSearchRequest;
import com.epam.indigoeln.eln.model.GlobalSearchResultDTO;
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
