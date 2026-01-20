package com.epam.indigoeln.compound.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class CompoundMapper {

//    public abstract Compound compoundToModel(CompoundRecord record);
//
//    public abstract CompoundDetails compoundToDetails(CompoundRecord record);
//    @Mapping(target = "id", ignore = true)
//    public abstract CompoundRecord compoundToRecord(CompoundDetails compound);
}
