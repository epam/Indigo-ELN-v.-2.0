package com.epam.indigoeln.compound.mapper;


import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SampleMapper {

    @Mapping(target = "name", source = "chemicalName")
    @Mapping(target = "compoundID", source = "compound.id")
    @Mapping(target = "molWeight", source = "compound.molWeight")
    @Mapping(target = "molecularFormula", source = "compound.formula")
    @Mapping(target = "saltCode", source = "compound.saltCode")
    @Mapping(target = "saltEQ", expression = "java(entity.getCompound().getSaltEQ100() != null ? entity.getCompound().getSaltEQ100() / 100.0 : null)")
    public abstract SampleDTO sampleToDTO(SampleEntity entity);
}
