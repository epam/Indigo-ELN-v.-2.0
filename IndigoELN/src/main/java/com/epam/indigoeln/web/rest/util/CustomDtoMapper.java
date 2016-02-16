package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Custom MapStruct mapper for converting DTO/Model objects
 */
@Mapper
public interface CustomDtoMapper {

    String ACCESS_LIST_MAPPER = "java(dto.getAccessList().stream()." +
            "map(s -> new com.epam.indigoeln.core.model.UserPermission(s.getUser(), s.getPermissions()))." +
            "collect(java.util.stream.Collectors.toSet()))";

    @Mapping(target = "authorities", ignore = true)
    User convertFromDTO(UserDTO userDTO);

    @Mapping(target = "authorities", ignore = true)
    User convertFromDTO(ManagedUserDTO userDTO);

    RoleDTO convertToDTO(Role role);

    Role convertFromDTO(RoleDTO roleDTO);

    Component convertFromDTO(ComponentDTO componentDTO);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    Experiment convertFromDTO(ExperimentDTO dto);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    Template convertFromDTO(TemplateDTO dto);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    Project convertFromDTO(ProjectDTO dto);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    Notebook convertFromDTO(NotebookDTO dto);
}