package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface MiscAPI extends BaseAPI {

    @GET
    @Path("/total-counts")
    TotalCounts getTotalCounts();

    @GET
    @Path("/dictionaries")
    List<Dictionary> getDictionaries();

    @GET
    @Path("/dictionaries/{dictionary}")
    List<DictionaryRef> getDictionary(@PathParam("dictionary") Dictionary dictionary);

    @GET
    @Path("/dictionaries/{dictionary}/full")
    List<DictionaryDTO> getDictionaryFull(@PathParam("dictionary") Dictionary dictionary);

    @PATCH
    @Path("/dictionaries/{dictionary}")
    List<DictionaryDTO> updateDictionary(@PathParam("dictionary") Dictionary dictionary, List<DictionaryRequest> content);

    @GET
    @Path("/saltCodes")
    List<DictionaryRef> getSaltCodes();

    @POST
    @Path("/users")
    UserRef getOrCreateUser(CreateUserForm form);

    @GET
    @Path("/users/suggest")
    List<UserRef> suggestUsers(@QueryParam("search") @Nullable String search, @BeanParam Paging paging);

    // TODO remove from API after Flyway is automated
    @POST
    @Path("/admin/flyway")
    Map<String, String> migrate();

    @POST
    @Path("/admin/cleanupDatabase")
    void cleanupDatabase();

    @POST
    @Path("/compounds/loadFromFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void loadCompoundsFromFile(UploadForm form);

    @POST
    @Path("/samples/search")
    List<SampleDTO> findSamples(FindSamplesRequest request);
}
