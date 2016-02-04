package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.model.User;
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

    Experiment convertFromDTO(ExperimentDTO dto);

    ExperimentDTO convertToDTO(Experiment experiment);

    Component convertFromDTO(ComponentDTO componentDTO);

    ComponentDTO convertToDTO(Component component);

    Template convertFromDTO(TemplateDTO templateDTO);
}