package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.model.NotebookDTO;
import com.epam.indigoeln.eln.model.NotebookDetailsDTO;
import com.epam.indigoeln.eln.model.NotebookRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class NotebookMapper extends AbstractMapper {

    @IgnoreBaseFields
    @Mapping(target = "searchVector", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "experiments", expression = "java(java.util.Set.of())")
    @Mapping(target = "aclEntities", expression = "java(java.util.Map.of())")
    @Mapping(target = "shortACL", ignore = true)
    @Mapping(target = "fullACL", ignore = true)
    @Mapping(target = "attachments", expression = "java(java.util.List.of())")
    @Mapping(target = "experimentCount", ignore = true)
    @Mapping(target = "calculatedInfo", ignore = true)
    public abstract NotebookEntity requestToNotebook(NotebookRequest notebook);

    @Mapping(target = "acl", source = "shortACL")
    @Mapping(target = "aclCount", source = "calculatedInfo.aclCount")
    public abstract NotebookDTO entityToDTO(NotebookEntity entity);

    @Mapping(target = "acl", source = "fullACL")
    public abstract NotebookDetailsDTO entityToDetailsDTO(NotebookEntity entity);
}
