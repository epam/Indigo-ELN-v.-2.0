package com.epam.indigoeln.compound.mapper;


import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.MarkedSampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.MOL_WEIGHT_DECIMAL_PLACES;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.roundToDecimalPlaces;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SampleMapper {

    @Inject
    DictionaryService dictionaryService;

    @Mapping(target = "catalog", constant = "MY_MATERIALS")
    @Mapping(target = "compoundKey", source = "compound.compoundKey")
    @Mapping(target = "compoundID", source = "compound.id")
    @Mapping(target = "molWeight", source = "compound.molWeight", qualifiedByName = "convertMolWeightLike")
    @Mapping(target = "molFormula", expression = "java(entity.getCompound().getFormula().toHTMLString())")
    @Mapping(target = "saltCode", expression = "java(dictionaryService.get(entity.getCompound().getSaltCode()))")
    @Mapping(target = "saltEQ", source = "entity.compound.saltEQ")
    @Mapping(target = "compoundState", expression = "java(dictionaryService.get(entity.getCompoundState()))")
    @Mapping(target = "chemicalName", source = "compound.chemicalName")
    @Mapping(target = "inchi", ignore = true)
    @Mapping(target = "marked", constant = "true")
    public abstract SampleDTO markedSampleToDTO(MarkedSampleEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "searchVector", ignore = true)
    @Mapping(target = "source", source = "dto.source")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "compoundState", source = "compoundState")
    public abstract MarkedSampleEntity markedSampleFromDTO(SampleDTO dto, UserEntity user, Instant createdAt, CompoundEntity compound, @Nullable DictionaryItemEntity compoundState);

    @Named("convertMolWeightLike")
    protected BigDecimal convertMolWeight(double value) {
        return roundToDecimalPlaces(value, MOL_WEIGHT_DECIMAL_PLACES);
    }

    @Nullable
    protected List<HealthHazardRef> convertHealthHazards(UUID @Nullable [] ids) {
        return ids != null ? dictionaryService.byId(List.of(ids)) : null;
    }

    protected UUID @Nullable [] convertHealthHazards(@Nullable List<HealthHazardRef> refs) {
        return refs != null ? StreamEx.of(refs).map(HealthHazardRef::getId).toArray(UUID[]::new) : null;
    }
}
