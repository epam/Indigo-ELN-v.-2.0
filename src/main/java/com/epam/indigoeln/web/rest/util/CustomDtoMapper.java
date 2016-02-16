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

    @Mapping(target = "authorities", ignore = true)
    User convertFromDTO(UserDTO userDTO);

    @Mapping(target = "authorities", ignore = true)
    User convertFromDTO(ManagedUserDTO userDTO);

    RoleDTO convertToDTO(Role role);

    Role convertFromDTO(RoleDTO roleDTO);

    @Mapping(target = "template", ignore = true)
    Experiment convertFromDTO(ExperimentDTO dto);

    @Mapping(target = "template", ignore = true)
    ExperimentDTO convertToDTO(Experiment experiment);

    Component convertFromDTO(ComponentDTO componentDTO);

    ComponentDTO convertToDTO(Component component);

    Template convertFromDTO(TemplateDTO templateDTO);

    Project convertFromDTO(ProjectDTO dto);

    ProjectDTO convertToDTO(Project project);

    Notebook convertFromDTO(NotebookDTO dto);

    NotebookDTO convertToDTO(Notebook notebook);
}