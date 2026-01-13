package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.ProjectRevisionEntity;
import com.epam.indigoeln.eln.entity.TotalCountsEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class ProjectMapper extends AbstractMapper {

    @Inject
    RevisionService revisionService;

    public abstract ProjectMutation.CreateProject requestToMutation(ProjectRequest request);
    public abstract ProjectMutation.EditProjectAttributes requestToMutation(ProjectEditRequest request);

    @Mapping(target = "acl", source = "shortACL")
    @Mapping(target = "aclCount", source = "calculatedInfo.aclCount")
    @Mapping(target = "experimentCountByStatus", source = "entity.experimentCount")
    public abstract ProjectDTO entityToDTO(ProjectEntity entity);

    @Mapping(target = "acl", source = "entity.fullACL")
    @Mapping(target = "experimentCountByStatus", source = "entity.experimentCount")
    public abstract ProjectDetailsDTO entityToDetailsDTO(ProjectEntity entity, Set<ApplicationPermission> currentPermissions);

    @Mapping(target = "experiments", expression = "java(convertMapToTotalCount(struct.getExperimentsByStatus()))")
    public abstract TotalCounts convertTotalCounts(TotalCountsEntity struct);

    @Nullable
    protected String dictionaryToString(@Nullable DictionaryItemEntity entity) {
        return entity != null ? entity.getName() : null;
    }

    @Mapping(target = "diff", expression = "java(convertPatch(entity))")
    public abstract RevisionDetailsDTO<ProjectPatch> revisionToDTO(ProjectRevisionEntity entity);
    public abstract List<RevisionDetailsDTO<ProjectPatch>> revisionToDTOList(List<ProjectRevisionEntity> entity);

    protected ProjectPatch convertPatch(ProjectRevisionEntity entity) {
        return revisionService.getPatch(entity);
    }
}
