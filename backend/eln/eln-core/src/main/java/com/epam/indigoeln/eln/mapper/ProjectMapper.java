package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.TotalCountsEntity;
import com.epam.indigoeln.eln.model.ProjectDTO;
import com.epam.indigoeln.eln.model.ProjectDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectRequest;
import com.epam.indigoeln.eln.model.TotalCounts;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ProjectMapper extends AbstractMapper {

    @IgnoreBaseFields
    @Mapping(target = "keywords", ignore = true)
    @Mapping(target = "searchVector", ignore = true)
    @Mapping(target = "currentAccess", ignore = true)
    @Mapping(target = "notebookCount", constant = "0")
    @Mapping(target = "experimentCount", expression = "java(java.util.Map.of())")
    @Mapping(target = "aclShort", expression = "java(new ACLEntry[0])")
    @Mapping(target = "aclCount", constant = "0")
    @Mapping(target = "aclEntities", expression = "java(java.util.Map.of())")
    @Mapping(target = "attachments", expression = "java(java.util.List.of())")
    @Mapping(target = "experiments", expression = "java(java.util.Set.of())")
    @Mapping(target = "notebooks", expression = "java(java.util.Set.of())")
    public abstract ProjectEntity requestToProject(ProjectRequest request);

    @Mapping(target = "acl", source = "aclShort")
    public abstract ProjectDTO entityToDTO(ProjectEntity entity);

    @Mapping(target = "acl", source = "aclEntities")
    @Mapping(target = "aclCount", expression = "java(entity.getAclEntities().size())")
    public abstract ProjectDetailsDTO entityToDetailsDTO(ProjectEntity entity);

    @Mapping(target = "experiments", expression = "java(convertTotalCountsSum(struct))")
    public abstract TotalCounts convertTotalCounts(TotalCountsEntity struct);

    protected Integer convertTotalCountsSum(TotalCountsEntity struct) {
        return struct.getExperimentsByStatus().values().stream().mapToInt(Integer::intValue).sum();
    }

    @Nullable
    protected String dictionaryToString(@Nullable DictionaryItemEntity entity) {
        return entity != null ? entity.getName() : null;
    }
}
