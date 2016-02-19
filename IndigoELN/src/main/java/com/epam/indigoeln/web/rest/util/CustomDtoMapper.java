package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.ManagedUserDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.RoleDTO;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import com.epam.indigoeln.web.rest.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

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
    @Mapping(target = "template", ignore = true)
    Experiment convertFromDTO(ExperimentDTO dto);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    Template convertFromDTO(TemplateDTO dto);

    @Mappings({
        @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER),
        @Mapping(target = "title", expression = "java(\"Project \" + dto.getName())")
    })
    Project convertFromDTO(ProjectDTO dto);

    @Mappings({
        @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER),
        @Mapping(target = "title", expression = "java(\"Notebook \" + dto.getName())")
    })
    Notebook convertFromDTO(NotebookDTO dto);
}