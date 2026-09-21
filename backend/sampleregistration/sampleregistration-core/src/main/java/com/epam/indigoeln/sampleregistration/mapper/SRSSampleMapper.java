package com.epam.indigoeln.sampleregistration.mapper;


import com.epam.indigoeln.sampleregistration.entity.SRSSampleEntity;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.MOL_WEIGHT_DECIMAL_PLACES;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.roundToDecimalPlaces;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SRSSampleMapper {

    @Mapping(target = "name", source = "compound.chemicalName")
    @Mapping(target = "compoundID", source = "compound.id")
    @Mapping(target = "strCodeCompound", source = "compound.strCode")
    @Mapping(target = "molWeight", source = "compound.molWeight", qualifiedByName = "convertMolWeightLike")
    @Mapping(target = "molFormula", expression = "java(entity.getCompound().getFormula().toHTMLString())")
    @Mapping(target = "saltCode", source = "compound.saltCode")
    @Mapping(target = "saltEQ", expression = "java(entity.getCompound().getSaltEQ100() != null ? entity.getCompound().getSaltEQ100() / 100.0 : null)")
    @Mapping(target = "inchi", ignore = true)
    @Mapping(target = "strCodeSample", source = "strCode")
    public abstract SRSSampleDTO sampleToDTO(SRSSampleEntity entity);

    @Named("convertMolWeightLike")
    protected BigDecimal convertMolWeight(double value) {
        return roundToDecimalPlaces(value, MOL_WEIGHT_DECIMAL_PLACES);
    }
}
