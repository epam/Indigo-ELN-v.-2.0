package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.DictionaryAPI;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import lombok.SneakyThrows;

import java.util.List;
import java.util.UUID;

public interface DictionaryClient extends DictionaryAPI {

    @SneakyThrows
    default <T extends DictionaryItemRef> List<T> getDictionary(BuiltInDictionary dictionary) {
        JsonNode node = getDictionaryRaw(dictionary.name());
        return FeignUtil.OBJECT_MAPPER.readerForListOf(dictionary.getRefClass()).readValue(node);
    }

    @GET
    @Path("/dictionaries/{dictionary}")
    JsonNode getDictionaryRaw(@PathParam("dictionary") String dictionaryRef);

    default <T extends DictionaryItemRef> T getFirst(BuiltInDictionary dictionary) {
        return this.<T>getDictionary(dictionary).getFirst();
    }

    default List<DictionaryItemDTO> getDictionaryFull(BuiltInDictionary dictionary) {
        return getDictionaryFull(dictionary.name());
    }

    default List<DictionaryItemRef> suggestDictionaryItems(BuiltInDictionary dictionary, @QueryParam("search") String search) {
        return suggestDictionaryItems(dictionary.name(), search);
    }

    default List<DictionaryItemDTO> addDictionaryItem(BuiltInDictionary dictionary, DictionaryItemRequest item) {
        return addDictionaryItem(dictionary.name(), item);
    }

    default List<DictionaryItemDTO> updateDictionaryItem(BuiltInDictionary dictionary, @PathParam("itemID") UUID itemID, DictionaryItemEditRequest request) {
        return updateDictionaryItem(dictionary.name(), itemID, request);
    }

    default List<DictionaryItemDTO> removeDictionaryItem(BuiltInDictionary dictionary, @PathParam("itemID") UUID itemID) {
        return removeDictionaryItem(dictionary.name(), itemID);
    }
}
