package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.UserDTO;
import com.epam.indigoeln.eln.model.UserDetailsDTO;
import com.epam.indigoeln.eln.model.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class UserMapper extends AbstractMapper {

    public abstract UserEntity requestToUser(UserRequest user);

    public abstract UserDTO entityToDTO(UserEntity entity);

    public abstract UserDetailsDTO entityToDetailsDTO(UserEntity entity);
}
