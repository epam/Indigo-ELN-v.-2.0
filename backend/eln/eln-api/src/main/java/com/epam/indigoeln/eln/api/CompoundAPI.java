package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.eln.model.TotalCounts;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;

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
}
