package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface DictionaryAPI extends BaseAPI {

    @GET
    @Path("/dictionaries")
    List<Dictionary> getDictionaries();

    @GET
    @Path("/dictionaries/{dictionary}")
    List<DictionaryItemRef> getDictionary(@PathParam("dictionary") Dictionary dictionary);

    @GET
    @Path("/dictionaries/{dictionary}/full")
    List<DictionaryItemDTO> getDictionaryFull(@PathParam("dictionary") Dictionary dictionary);

    @PATCH
    @Path("/dictionaries/{dictionary}")
    List<DictionaryItemDTO> updateDictionary(@PathParam("dictionary") Dictionary dictionary, List<DictionaryItemRequest> content);

    @GET
    @Path("/saltCodes")
    List<DictionaryItemRef> getSaltCodes();
}
