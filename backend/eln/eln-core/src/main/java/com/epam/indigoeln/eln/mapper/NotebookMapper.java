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
    @Mapping(target = "currentAccess", ignore = true)
    @Mapping(target = "experimentCount", expression = "java(java.util.Map.of())")
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "experiments", expression = "java(java.util.Set.of())")
    @Mapping(target = "aclShort", expression = "java(new ACLEntry[0])")
    @Mapping(target = "aclCount", constant = "0")
    @Mapping(target = "aclEntities", expression = "java(java.util.Map.of())")
    @Mapping(target = "attachments", expression = "java(java.util.List.of())")
    public abstract NotebookEntity requestToNotebook(NotebookRequest notebook);

    @Mapping(target = "acl", source = "aclShort")
    public abstract NotebookDTO entityToDTO(NotebookEntity entity);

    @Mapping(target = "acl", source = "aclEntities")
    @Mapping(target = "aclCount", expression = "java(entity.getAclEntities().size())")
    public abstract NotebookDetailsDTO entityToDetailsDTO(NotebookEntity entity);
}
