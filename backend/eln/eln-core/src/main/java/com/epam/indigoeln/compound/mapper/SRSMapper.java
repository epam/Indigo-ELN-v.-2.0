package com.epam.indigoeln.compound.mapper;


import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.common.model.NbkBatchNumber;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SRSMapper {

    @Inject
    DictionaryService dictionaryService;

    @Mapping(target = "strCodeCompound", source = "compoundKey")
    @Mapping(target = "strCodeSample", source = "sampleKey")
    public abstract SRSFindSamplesRequest requestToSRS(FindSamplesRequest request);

    @Mapping(target = "catalog", constant = "SRS")
    @Mapping(target = "source", constant = "SRS")
    @Mapping(target = "compoundKey", expression = "java(sample.getStrCodeCompound().toString())")
    @Mapping(target = "sampleKey", expression = "java(sample.getStrCodeSample().toString())")
    @Mapping(target = "saltCode", expression = "java(sample.getSaltCode() != null ? dictionaryService.byId(sample.getSaltCode()) : null)")
    @Mapping(target = "healthHazards", expression = "java(dictionaryService.byId(sample.getHealthHazards()))")
    @Mapping(target = "compoundState", expression = "java(sample.getCompoundState() != null ? dictionaryService.byId(sample.getCompoundState()) : null)")
    @Mapping(target = "inchi", ignore = true)
    @Mapping(target = "marked", constant = "false")
    public abstract SampleDTO sampleFromSRS(SRSSampleDTO sample);

    @Nullable
    protected UUID mapDictionaryRef(@Nullable DictionaryItemRef ref) {
        return ref != null ? ref.getId() : null;
    }

    @Nullable
    protected NbkBatchNumber nbkBatchNumberFromString(@Nullable String string) {
        return string != null ? NbkBatchNumber.parse(string) : null;
    }
}
