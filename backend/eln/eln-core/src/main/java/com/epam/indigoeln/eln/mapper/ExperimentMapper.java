package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
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
    public abstract ExperimentDetailsDTO entityToDetailsDTO(ExperimentEntity entity, ExperimentModel model, Set<ApplicationPermission> currentPermissions);

    @Mapping(target = "diff", expression = "java(convertPatch(entity))")
    @Mapping(target = "stringDiff", expression = "java(revisionService.formatPatch(entity.getDiff()))")
    public abstract RevisionDetailsDTO<ExperimentPatch> revisionToDTO(ExperimentRevisionEntity entity);
    public abstract List<RevisionDetailsDTO<ExperimentPatch>> revisionToDTOList(List<ExperimentRevisionEntity> entity);

    protected ExperimentPatch convertPatch(ExperimentRevisionEntity entity) {
        return revisionService.getPatch(entity);
    }
}
