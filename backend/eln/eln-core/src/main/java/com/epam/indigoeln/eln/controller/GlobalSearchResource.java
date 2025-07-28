package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.GlobalSearchAPI;
import com.epam.indigoeln.eln.api.MiscAPI;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.GlobalSearchService;
import com.epam.indigoeln.eln.service.ProjectService;
import com.epam.indigoeln.eln.service.SupportService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;

import java.util.Map;

@Path(BaseAPI.BASE_PATH)
public class GlobalSearchResource implements GlobalSearchAPI {

    @Inject
    GlobalSearchService globalSearchService;

    @Override
    public Page<GlobalSearchResultDTO> search(GlobalSearchRequest request, Paging paging) {
        return globalSearchService.search(request, paging);
    }
}
