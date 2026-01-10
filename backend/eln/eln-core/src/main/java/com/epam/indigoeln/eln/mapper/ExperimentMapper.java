package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ExperimentDTO;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentRevisionDetailsDTO;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
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
    ExperimentModelService experimentModelService;

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
    public abstract ExperimentRevisionDetailsDTO revisionToDTO(ExperimentRevisionEntity entity);
    public abstract List<ExperimentRevisionDetailsDTO> revisionToDTOList(List<ExperimentRevisionEntity> entity);

    protected ExperimentPatch convertPatch(ExperimentRevisionEntity entity) {
        return experimentModelService.getPatch(entity);
    }
}
