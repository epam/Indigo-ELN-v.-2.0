package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.DictionaryAPI;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.DictionaryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class DictionaryResource implements DictionaryAPI {

    @Inject
    DictionaryService dictionaryService;

    @Override
    public List<Dictionary> getDictionaries() {
        return dictionaryService.getDictionaries();
    }

    @Override
    public List<DictionaryItemRef> getDictionary(Dictionary dictionary) {
        return dictionaryService.getDictionary(dictionary);
    }

    @Override
    public List<DictionaryItemDTO> getDictionaryFull(Dictionary dictionary) {
        return dictionaryService.getDictionaryFull(dictionary);
    }

    @Override
    public List<DictionaryItemDTO> addDictionaryItem(Dictionary dictionary, DictionaryItemRequest item) {
        return dictionaryService.addDictionaryItem(dictionary, item);
    }

    @Override
    public List<DictionaryItemDTO> updateDictionaryItem(Dictionary dictionary, UUID itemID, DictionaryItemEditRequest request) {
        return dictionaryService.updateDictionaryItem(dictionary, itemID, request);
    }

    @Override
    public List<DictionaryItemDTO> removeDictionaryItem(Dictionary dictionary, UUID itemID) {
        return dictionaryService.removeDictionaryItem(dictionary, itemID);
    }

    @Override
    public List<DictionaryItemRef> getSaltCodes() {
        return dictionaryService.getSaltCodes();
    }
}
