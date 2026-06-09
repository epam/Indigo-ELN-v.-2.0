package com.epam.indigoeln.compound.service.search.pubchem;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PubChemResponse(
        @JsonProperty("PropertyTable") PropertyTable propertyTable
) {
    public record PropertyTable(
            @JsonProperty("Properties") List<Item> items
    ) {
    }

    public record Item(
            @JsonProperty("CID") Integer cid,
            @JsonProperty("MolecularFormula") String molFormula,
            @JsonProperty("MolecularWeight") Double molWeight,
            @JsonProperty("InChI") String inchi,
            @JsonProperty("IUPACName") String name
    ) {
    }
}
