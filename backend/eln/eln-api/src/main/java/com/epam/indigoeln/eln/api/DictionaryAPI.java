package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Dictionary;
import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface DictionaryAPI extends BaseAPI {

    @GET
    @Path("/dictionaries")
    List<DictionaryDTO> getDictionaries();

    @GET
    @Path("/dictionaries/{dictionary}")
    List<DictionaryItemRef> getDictionary(@PathParam("dictionary") String dictionaryRef);

    @GET
    @Path("/dictionaries/{dictionary}/full")
    List<DictionaryItemDTO> getDictionaryFull(@PathParam("dictionary") String dictionaryRef);

    @GET
    @Path("/dictionaries/{dictionary}/suggest")
    List<DictionaryItemRef> suggestDictionaryItems(@PathParam("dictionary") String dictionaryRef, @QueryParam("search") String search);

    @POST
    @Path("/dictionaries/{dictionary}")
    List<DictionaryItemDTO> addDictionaryItem(@PathParam("dictionary") String dictionaryRef, DictionaryItemRequest item);

    @PATCH
    @Path("/dictionaries/{dictionary}/{itemID}")
    List<DictionaryItemDTO> updateDictionaryItem(@PathParam("dictionary") String dictionaryRef, @PathParam("itemID") UUID itemID, DictionaryItemEditRequest request);

    @DELETE
    @Path("/dictionaries/{dictionary}/{itemID}")
    List<DictionaryItemDTO> removeDictionaryItem(@PathParam("dictionary") String dictionaryRef, @PathParam("itemID") UUID itemID);

    @GET
    @Path("/saltCodes")
    List<DictionaryItemRef> getSaltCodes();
}
