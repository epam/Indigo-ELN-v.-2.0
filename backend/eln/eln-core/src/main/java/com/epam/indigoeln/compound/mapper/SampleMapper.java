package com.epam.indigoeln.compound.mapper;


import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.service.DictionaryService;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.MOL_WEIGHT_DECIMAL_PLACES;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.roundToDecimalPlaces;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SampleMapper {

    @Inject
    DictionaryService dictionaryService;

    @Mapping(target = "source", expression = "java(entity.getMarked() == Boolean.TRUE ? com.epam.indigoeln.compound.model.search.SearchCatalog.MY_MATERIALS : com.epam.indigoeln.compound.model.search.SearchCatalog.ELN)")
    @Mapping(target = "name", source = "compound.chemicalName")
    @Mapping(target = "compoundID", source = "compound.id")
    @Mapping(target = "compoundKey", source = "compound.compoundKey")
    @Mapping(target = "molWeight", source = "compound.molWeight", qualifiedByName = "convertMolWeightLike")
    @Mapping(target = "molFormula", expression = "java(entity.getCompound().getFormula().toString())")
    @Mapping(target = "saltCode", expression = "java(dictionaryService.get(entity.getCompound().getSaltCode()))")
    @Mapping(target = "saltEQ", source = "entity.compound.saltEQ")
    @Mapping(target = "marked", expression = "java(entity.getMarked() == Boolean.TRUE)")
    @Mapping(target = "inchi", ignore = true)
    public abstract SampleDTO sampleToDTO(SampleEntity entity);

    @Named("convertMolWeightLike")
    protected BigDecimal convertMolWeight(double value) {
        return roundToDecimalPlaces(value, MOL_WEIGHT_DECIMAL_PLACES);
    }
}
