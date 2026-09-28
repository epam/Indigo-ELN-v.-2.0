package com.epam.indigoeln.compound.service.search.pubchem;

import com.epam.indigoeln.compound.model.SampleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
interface PubChemMapper {

    @Mapping(target = "catalog", constant = "PUBCHEM")
    @Mapping(target = "source", constant = "PUBCHEM")
    @Mapping(target = "nbkBatchNumber", ignore = true)
    @Mapping(target = "compoundKey", source = "cid")
    @Mapping(target = "compoundID", ignore = true)
    @Mapping(target = "chemicalName", source = "name")
    @Mapping(target = "sampleKey", source = "cid")
    @Mapping(target = "saltCode", ignore = true)
    @Mapping(target = "saltEQ", ignore = true)
    @Mapping(target = "density", ignore = true)
    @Mapping(target = "molarity", ignore = true)
    @Mapping(target = "molarityUnit", ignore = true)
    @Mapping(target = "purity", ignore = true)
    @Mapping(target = "healthHazards", ignore = true)
    @Mapping(target = "compoundState", ignore = true)
    @Mapping(target = "batchComment", ignore = true)
    @Mapping(target = "marked", constant = "false")
    SampleDTO mapSample(PubChemResponse.Item item);

    List<SampleDTO> mapSamples(List<PubChemResponse.Item> item);
}
