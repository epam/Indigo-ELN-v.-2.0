package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.ExperimentDTO;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentRequest;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class ExperimentMapper extends AbstractMapper {

    @IgnoreBaseFields
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "notebook", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "therapeuticArea", ignore = true)
    @Mapping(target = "projectCode", ignore = true)
    @Mapping(target = "searchVector", ignore = true)
    @Mapping(target = "reportForSignature", ignore = true)
    @Mapping(target = "aclEntities", expression = "java(java.util.Map.of())")
    @Mapping(target = "shortACL", ignore = true)
    @Mapping(target = "fullACL", ignore = true)
    @Mapping(target = "attachments", expression = "java(java.util.List.of())")
    @Mapping(target = "signatures", expression = "java(java.util.List.of())")
    @Mapping(target = "referencedCompounds", expression = "java(java.util.Set.of())")
    @Mapping(target = "referencedDictionaryItemIDs", expression = "java(java.util.Set.of())")
    @Mapping(target = "rxnfiles", expression = "java(java.util.List.of())")
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "picture", ignore = true)
    @Mapping(target = "calculatedInfo", ignore = true)
    public abstract ExperimentEntity requestToExperiment(ExperimentRequest experiment, ExperimentStatus status);

    @Mapping(target = "acl", source = "shortACL")
    @Mapping(target = "aclCount", source = "calculatedInfo.aclCount")
    @Mapping(target = "marked", source = "calculatedInfo.marked")
    public abstract ExperimentDTO entityToDTO(ExperimentEntity entity);

    @Mapping(target = "acl", source = "fullACL")
    @Mapping(target = "marked", source = "calculatedInfo.marked")
    @Mapping(target = "templateId", source = "template.id")
    public abstract ExperimentDetailsDTO entityToDetailsDTO(ExperimentEntity entity);
}
