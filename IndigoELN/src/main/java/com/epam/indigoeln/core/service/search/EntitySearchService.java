package com.epam.indigoeln.core.service.search;

import com.epam.indigoeln.core.repository.search.entity.EntitySearchRepository;
import com.epam.indigoeln.web.rest.dto.search.EntitySearchResultDTO;
import com.epam.indigoeln.web.rest.dto.search.request.EntitySearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntitySearchService {

    @Autowired
    private EntitySearchRepository repository;

    public List<EntitySearchResultDTO> find(EntitySearchRequest searchRequest) {
        return repository.findEntities(searchRequest);
    }

}
