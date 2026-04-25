package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class ExperimentMapper extends AbstractMapper {

    @Inject
    RevisionService revisionService;

    public abstract ExperimentMutation.CreateExperiment requestToMutation(ExperimentRequest request);
    public abstract ExperimentMutation.EditExperimentAttributes requestToMutation(ExperimentEditRequest request);
    
    @Mapping(target = "acl", source = "shortACL")
    @Mapping(target = "aclCount", source = "calculatedInfo.aclCount")
    @Mapping(target = "marked", source = "calculatedInfo.marked")
    public abstract ExperimentDTO entityToDTO(ExperimentEntity entity);

    @Mapping(target = "acl", source = "entity.fullACL")
    @Mapping(target = "marked", source = "entity.calculatedInfo.marked")
    @Mapping(target = "templateId", source = "entity.template.id")
    @Mapping(target = "model", source = "model")
    @Mapping(target = "projectId", source = "entity.project.id")
    @Mapping(target = "projectName", source = "entity.project.name")
    @Mapping(target = "notebookId", source = "entity.notebook.id")
    @Mapping(target = "notebookName", source = "entity.notebook.name")
    public abstract ExperimentDetailsDTO entityToDetailsDTO(ExperimentEntity entity, ExperimentModel model, Set<ApplicationPermission> currentPermissions);

    @Mapping(target = "diff", expression = "java(revisionService.getPatch(entity))")
    @Mapping(target = "stringDiff", expression = "java(revisionService.formatPatch(entity.getDiff()))")
    public abstract RevisionDetailsDTO revisionToDTO(ExperimentRevisionEntity entity);
    public abstract List<RevisionDetailsDTO> revisionToDTOList(List<ExperimentRevisionEntity> entity);
}
