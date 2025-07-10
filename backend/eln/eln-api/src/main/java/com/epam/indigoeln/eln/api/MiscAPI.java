package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface MiscAPI extends BaseAPI {

    @GET
    @Path("/total-counts")
    TotalCounts getTotalCounts();

    // TODO remove from API after Flyway is automated
    @POST
    @Path("/admin/flyway")
    Map<String, String> migrate();
}
