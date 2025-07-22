package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.UserDTO;
import com.epam.indigoeln.eln.model.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class UserMapper extends AbstractMapper {

    @IgnoreBaseFields
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "displayName", expression = "java(com.epam.indigoeln.common.util.ModelUtil.formatUser(user.getFirstName(), user.getLastName(), user.getUsername()))")
    public abstract UserEntity requestToUser(UserRequest user);

    public abstract UserDTO entityToDTO(UserEntity entity);

    public abstract UserDTO entityToDetailsDTO(UserEntity entity);
}
