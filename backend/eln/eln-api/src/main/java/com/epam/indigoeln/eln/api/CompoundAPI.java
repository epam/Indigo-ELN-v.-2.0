package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.eln.model.SampleSource;
import com.epam.indigoeln.eln.quarkus.cachecontrol.Cached;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CompoundAPI extends BaseAPI {

    @GET
    @Path("/compounds/{compoundID}/molfile")
    @Produces(MediaType.TEXT_PLAIN)
    @Cached(interval = 30, unit = ChronoUnit.DAYS)
    String getCompoundMolfile(@PathParam("compoundID") UUID compoundID);

    @GET
    @Path("/compounds/by-catalog/{catalog}/{source}/{compoundID}/molfile")
    @Produces(MediaType.TEXT_PLAIN)
    @Cached(interval = 30, unit = ChronoUnit.DAYS)
    String getCatalogCompoundMolfile(@PathParam("catalog") SearchCatalog catalog, @PathParam("source") SampleSource source, @PathParam("compoundID") UUID compoundID);

    @POST
    @Path("/samples/search")
    Page<@Valid SampleDTO> search(@Valid FindSamplesRequest request, @BeanParam Paging paging);

    @POST
    @Valid
    @Path("/samples/mark")
    SampleDTO markSample(@Valid SampleDTO sample);

    @POST
    @Valid
    @Path("/samples/unmark")
    SampleDTO unmarkSample(@Valid SampleDTO sample);
}
