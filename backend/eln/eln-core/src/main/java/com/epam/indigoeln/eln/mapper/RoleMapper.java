package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.RoleEntity;
import com.epam.indigoeln.eln.model.RoleDTO;
import com.epam.indigoeln.eln.model.RoleRef;
import com.epam.indigoeln.eln.model.RoleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class RoleMapper extends AbstractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", expression = "java(new ApplicationPermission[0])")
    public abstract RoleEntity requestToRole(RoleRequest user);

    public abstract RoleDTO entityToDTO(RoleEntity entity);

    public abstract RoleRef entityToRef(RoleEntity entity);
}
