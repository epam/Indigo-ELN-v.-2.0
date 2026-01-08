package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ExperimentDTO;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentRevisionDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class ExperimentMapper extends AbstractMapper {

    @Mapping(target = "acl", source = "shortACL")
    @Mapping(target = "aclCount", source = "calculatedInfo.aclCount")
    @Mapping(target = "marked", source = "calculatedInfo.marked")
    public abstract ExperimentDTO entityToDTO(ExperimentEntity entity);

    @Mapping(target = "acl", source = "entity.fullACL")
    @Mapping(target = "marked", source = "entity.calculatedInfo.marked")
    @Mapping(target = "templateId", source = "entity.template.id")
    public abstract ExperimentDetailsDTO entityToDetailsDTO(ExperimentEntity entity, Set<ApplicationPermission> currentPermissions);

    public abstract ExperimentRevisionDetailsDTO revisionToDTO(ExperimentRevisionEntity entity);
    public abstract List<ExperimentRevisionDetailsDTO> revisionToDTOList(List<ExperimentRevisionEntity> entity);
}
