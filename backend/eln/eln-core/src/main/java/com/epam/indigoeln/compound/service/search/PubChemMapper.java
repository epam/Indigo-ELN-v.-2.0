package com.epam.indigoeln.compound.service.search;

import com.epam.indigoeln.compound.model.SampleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
interface PubChemMapper {

    @Mapping(target = "source", constant = "PUBCHEM")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nbkBatchNumber", ignore = true)
    @Mapping(target = "compoundKey", source = "cid")
    @Mapping(target = "strCode", ignore = true)
    @Mapping(target = "saltCode", ignore = true)
    @Mapping(target = "saltEQ", ignore = true)
    @Mapping(target = "compoundID", ignore = true)
    @Mapping(target = "marked", constant = "false")
    SampleDTO mapSample(PubChemResponse.Item item);

    List<SampleDTO> mapSamples(List<PubChemResponse.Item> item);
}
