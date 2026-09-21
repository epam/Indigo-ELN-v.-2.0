package com.epam.indigoeln.sampleregistration.api;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationRequest;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path(SampleRegistrationAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SampleRegistrationAPI {

    String BASE_PATH = "/api/sampleregistration";

    @POST
    @Path("/register")
    SampleRegistrationResponse registerSample(SampleRegistrationRequest request);

    @POST
    @Path("/search")
    Page<SRSSampleDTO> find(SRSFindSamplesRequest request, @QueryParam("pageNo") int pageNo, @QueryParam("pageSize") int pageSize);
}
