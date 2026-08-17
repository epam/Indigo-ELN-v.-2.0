package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.MiscInfo;
import com.epam.indigoeln.eln.model.TotalCounts;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface MiscAPI extends BaseAPI {

    @GET
    @Path("/info")
    MiscInfo getInfo();

    @GET
    @Path("/total-counts")
    TotalCounts getTotalCounts();

    // TODO remove from API after Flyway is automated
    @POST
    @Path("/admin/flyway")
    Map<String, String> migrate();

    @POST
    @Path("/admin/insertTestData")
    Map<String, String> insertTestData();

    @POST
    @Path("/support/reindex-search-vectors")
    Map<String, String> reindexSearchVectors();

    @POST
    @Path("/support/experiment-details-report/{experimentID}")
    @Produces(MediaType.TEXT_HTML)
    Response generateExperimentDetailsReport(@PathParam("experimentID") UUID experimentID);
}
