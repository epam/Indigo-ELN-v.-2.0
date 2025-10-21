package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CompoundAPI extends BaseAPI {

    @POST
    @Path("/compounds/loadFromFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void loadCompoundsFromFile(UploadForm form);

    @POST
    @Path("/samples/search")
    List<SampleDTO> findSamples(FindSamplesRequest request);

    @POST
    @Path("/samples/{sampleID}/mark")
    void markSample(@PathParam("sampleID") UUID sampleID);

    @POST
    @Path("/samples/{sampleID}/unmark")
    void unmarkSample(@PathParam("sampleID") UUID sampleID);

    @GET
    @Path("/samples/marked")
    Page<SampleDTO> getMarkedSamples(@Nullable @QueryParam("search") String search, @BeanParam Paging paging);
}
