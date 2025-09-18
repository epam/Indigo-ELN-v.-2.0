package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.DictionaryAPI;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
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
    @Inject
    DictionaryMapper dictionaryMapper;

    @Override
    public List<DictionaryDTO> getDictionaries() {
        return dictionaryService.getDictionaries();
    }

    @Override
    public List<DictionaryItemRef> getDictionary(String dictionaryRef) {
        return dictionaryService.getDictionary(dictionaryRef);
    }

    @Override
    public List<DictionaryItemDTO> getDictionaryFull(String dictionaryRef) {
        return dictionaryService.getDictionaryFull(dictionaryRef);
    }

    @Override
    public List<DictionaryItemRef> suggestDictionaryItems(String dictionaryRef, String search) {
        return dictionaryService.suggestDictionaryItems(dictionaryRef, search);
    }

    @Override
    public List<DictionaryItemDTO> addDictionaryItem(String dictionaryRef, DictionaryItemRequest item) {
        return dictionaryMapper.itemToDTOList(dictionaryService.addDictionaryItems(dictionaryRef, List.of(item)));
    }

    @Override
    public List<DictionaryItemDTO> updateDictionaryItem(String dictionaryRef, UUID itemID, DictionaryItemEditRequest request) {
        return dictionaryService.updateDictionaryItem(dictionaryRef, itemID, request);
    }

    @Override
    public List<DictionaryItemDTO> removeDictionaryItem(String dictionaryRef, UUID itemID) {
        return dictionaryService.removeDictionaryItem(dictionaryRef, itemID);
    }

    @Override
    public List<DictionaryItemRef> getSaltCodes() {
        return dictionaryService.getSaltCodes();
    }
}
