package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jspecify.annotations.Nullable;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CompoundAPI extends BaseAPI {

    @POST
    @Path("/compounds/loadFromFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void loadCompoundsFromFile(UploadForm form);

    @GET
    @Path("/compounds/{compoundID}/picture")
    @Produces("image/svg+xml")
    @Cached(interval = 30, unit = ChronoUnit.DAYS)
    byte[] getCompoundPicture(@PathParam("compoundID") UUID compoundID);

    @POST
    @Path("/samples/search")
    SampleSearchResult search(@Valid FindSamplesRequest request, @Nullable @QueryParam("nextCatalog") SearchCatalog nextCatalog, @Nullable @QueryParam("nextAfter") String nextAfter, @Nullable @QueryParam("limit") Integer limit);

    @POST
    @Path("/samples/importFromSearch")
    SampleDTO importFromSearch(SampleDTO searchItem);

    @GET
    @Path("/samples/external/picture")
    @Produces("image/svg+xml")
    @Cached(interval = 30, unit = ChronoUnit.DAYS)
    byte[] getExternalPicture(@QueryParam("inchi") String inchi);

    @POST
    @Path("/samples/{sampleID}/mark")
    SampleDTO markSample(@PathParam("sampleID") UUID sampleID);

    @POST
    @Path("/samples/{sampleID}/unmark")
    SampleDTO unmarkSample(@PathParam("sampleID") UUID sampleID);
}
