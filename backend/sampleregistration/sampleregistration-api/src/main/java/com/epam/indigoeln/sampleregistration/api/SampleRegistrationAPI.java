package com.epam.indigoeln.sampleregistration.api;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.sampleregistration.model.SRSCompoundDTO;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationRequest;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path(SampleRegistrationAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SampleRegistrationAPI {

    String BASE_PATH = "/api/sampleregistration";

    @POST
    @Path("/register")
    SampleRegistrationResponse registerSample(@Valid SampleRegistrationRequest request);

    @POST
    @Path("/search")
    Page<@Valid SRSSampleDTO> find(@Valid SRSFindSamplesRequest request, @BeanParam Paging paging);

    @GET
    @Path("/compounds/{id}")
    SRSCompoundDTO getCompound(@PathParam("id") UUID id);

    @GET
    @Path("/compounds/{id}/picture")
    @Produces("image/svg+xml")
    byte[] getCompoundPicture(@PathParam("id") UUID id);

    @POST
    @Path("/compounds/loadFromFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    int loadCompoundsFromFile(UploadForm form);
}
