package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.DictionaryAPI;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.DictionaryUpdateService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class DictionaryResource implements DictionaryAPI {

    @Inject
    DictionaryService dictionaryService;
    @Inject
    DictionaryUpdateService dictionaryUpdateService;
    @Inject
    DictionaryMapper dictionaryMapper;

    @Override
    public List<DictionaryDTO> getDictionaries() {
        return dictionaryUpdateService.getDictionaries();
    }

    @Override
    public DictionaryDTO createDictionary(DictionaryRequest request) {
        return dictionaryUpdateService.createDictionary(request);
    }

    @Override
    public DictionaryDTO updateDictionary(String dictionaryRef, DictionaryEditRequest request) {
        return dictionaryUpdateService.updateDictionary(dictionaryRef, request);
    }

    @Override
    public void removeDictionary(String dictionaryRef) {
        dictionaryUpdateService.removeDictionary(dictionaryRef);
    }

    @Override
    public List<DictionaryItemRef> getDictionary(String dictionaryRef) {
        return dictionaryService.getDictionary(dictionaryRef, false);
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
        return dictionaryMapper.itemToDTOList(dictionaryUpdateService.addDictionaryItems(dictionaryRef, List.of(item)));
    }

    @Override
    public List<DictionaryItemDTO> updateDictionaryItem(String dictionaryRef, UUID itemID, DictionaryItemEditRequest request) {
        return dictionaryUpdateService.updateDictionaryItem(dictionaryRef, itemID, request);
    }

    @Override
    public List<DictionaryItemDTO> removeDictionaryItem(String dictionaryRef, UUID itemID) {
        return dictionaryUpdateService.removeDictionaryItem(dictionaryRef, itemID);
    }
}
