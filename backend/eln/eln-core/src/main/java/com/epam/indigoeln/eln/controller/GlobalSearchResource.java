package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.GlobalSearchAPI;
import com.epam.indigoeln.eln.model.GlobalSearchRequest;
import com.epam.indigoeln.eln.model.GlobalSearchResultDTO;
import com.epam.indigoeln.eln.service.GlobalSearchService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;

@Path(BaseAPI.BASE_PATH)
public class GlobalSearchResource implements GlobalSearchAPI {

    @Inject
    GlobalSearchService globalSearchService;

    @Override
    public Page<@Valid GlobalSearchResultDTO> search(GlobalSearchRequest request, Paging paging) {
        return globalSearchService.search(request, paging);
    }
}
