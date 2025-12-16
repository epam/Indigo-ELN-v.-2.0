package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.DictionaryAPI;
import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;

import java.util.List;
import java.util.UUID;

public interface DictionaryClient extends DictionaryAPI {

    default List<DictionaryItemRef> getDictionary(BuiltInDictionary dictionary) {
        return getDictionary(dictionary.name());
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
