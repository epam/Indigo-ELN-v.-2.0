package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

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

    @POST
    @Path("/dictionaries/{dictionary}")
    List<DictionaryItemDTO> addDictionaryItem(@PathParam("dictionary") Dictionary dictionary, DictionaryItemRequest item);

    @PATCH
    @Path("/dictionaries/{dictionary}/{itemID}")
    List<DictionaryItemDTO> updateDictionaryItem(@PathParam("dictionary") Dictionary dictionary, @PathParam("itemID") UUID itemID, DictionaryItemEditRequest request);

    @DELETE
    @Path("/dictionaries/{dictionary}/{itemID}")
    List<DictionaryItemDTO> removeDictionaryItem(@PathParam("dictionary") Dictionary dictionary, @PathParam("itemID") UUID itemID);

    @GET
    @Path("/saltCodes")
    List<DictionaryItemRef> getSaltCodes();
}
