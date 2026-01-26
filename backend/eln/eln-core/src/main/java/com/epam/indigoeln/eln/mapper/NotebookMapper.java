package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.NotebookRevisionEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class NotebookMapper extends AbstractMapper {

    @Inject
    RevisionService revisionService;

    public abstract NotebookMutation.CreateNotebook requestToMutation(NotebookRequest request);
    public abstract NotebookMutation.EditNotebookAttributes requestToMutation(NotebookEditRequest request);

    @Mapping(target = "acl", source = "shortACL")
    @Mapping(target = "aclCount", source = "calculatedInfo.aclCount")
    @Mapping(target = "experimentCountByStatus", source = "entity.experimentCount")
    public abstract NotebookDTO entityToDTO(NotebookEntity entity);

    @Mapping(target = "acl", source = "entity.fullACL")
    @Mapping(target = "experimentCountByStatus", source = "entity.experimentCount")
    public abstract NotebookDetailsDTO entityToDetailsDTO(NotebookEntity entity, Set<ApplicationPermission> currentPermissions);

    @Mapping(target = "diff", expression = "java(revisionService.getPatch(entity))")
    @Mapping(target = "stringDiff", expression = "java(revisionService.formatPatch(entity.getDiff()))")
    public abstract RevisionDetailsDTO<NotebookPatch> revisionToDTO(NotebookRevisionEntity entity);
    public abstract List<RevisionDetailsDTO<NotebookPatch>> revisionToDTOList(List<NotebookRevisionEntity> entity);
}
