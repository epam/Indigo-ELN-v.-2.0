package com.epam.indigoeln.compound.mapper;


import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.service.DictionaryService;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SampleMapper {

    @Inject
    DictionaryService dictionaryService;

    @Mapping(target = "source", expression = "java(entity.getMarked() == Boolean.TRUE ? com.epam.indigoeln.compound.model.search.SearchCatalog.MY_MATERIALS : com.epam.indigoeln.compound.model.search.SearchCatalog.ELN)")
    @Mapping(target = "name", source = "compound.chemicalName")
    @Mapping(target = "compoundID", source = "compound.id")
    @Mapping(target = "compoundKey", source = "compound.compoundKey")
    @Mapping(target = "molWeight", source = "compound.molWeight")
    @Mapping(target = "molFormula", expression = "java(com.epam.indigoeln.eln.util.MolFormulaFormatter.format(entity.getCompound().getFormula()))")
    @Mapping(target = "saltCode", expression = "java(dictionaryService.get(entity.getCompound().getSaltCode()))")
    @Mapping(target = "saltEQ", expression = "java(entity.getCompound().getSaltEQ100() != null ? entity.getCompound().getSaltEQ100() / 100.0 : null)")
    @Mapping(target = "marked", expression = "java(entity.getMarked() == Boolean.TRUE)")
    @Mapping(target = "inchi", ignore = true)
    public abstract SampleDTO sampleToDTO(SampleEntity entity);
}
