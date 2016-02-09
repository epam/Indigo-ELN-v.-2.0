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

    Experiment convertFromDTO(ExperimentDTO dto);

    ExperimentDTO convertToDTO(Experiment experiment);

    Component convertFromDTO(ComponentDTO componentDTO);

    ComponentDTO convertToDTO(Component component);

    Template convertFromDTO(TemplateDTO templateDTO);

    Project convertFromDTO(ProjectDTO dto);

    ProjectDTO convertToDTO(Project project);

    Notebook convertFromDTO(NotebookDTO dto);

    NotebookDTO convertToDTO(Notebook notebook);
}