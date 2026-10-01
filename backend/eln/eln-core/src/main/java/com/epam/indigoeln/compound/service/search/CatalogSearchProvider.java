package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.eln.model.SampleSource;

import java.util.UUID;

public interface CatalogSearchProvider {

    SearchCatalog catalog();

    Page<SampleDTO> search(FindSamplesRequest request, Paging paging);

    CompoundEntity importCompound(SampleDTO sample);

    /** A compound's picture by its id in `source` — for a catalog whose compounds the ELN does not hold. */
    default byte[] getCompoundPicture(SampleSource source, UUID compoundID) {
        throw new EntityNotFoundException("Compound", compoundID);
    }
}
