package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Batch;
import com.epam.indigoeln.core.model.ComponentTemplate;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentTemplate;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.web.rest.dto.BatchDTO;
import com.epam.indigoeln.web.rest.dto.ComponentTemplateDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentTemplateDTO;
import com.epam.indigoeln.web.rest.dto.ManagedUserDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Custom MapStruct mapper for converting DTO/Model objects
 */
@Mapper
public interface CustomDtoMapper {

    String AUTHORITIES_MAPPER = "java(userDTO.getAuthorities().stream()." +
                                "map(s -> new com.epam.indigoeln.core.model.Authority(s))." +
                                "collect(java.util.stream.Collectors.toSet()))";

    @Mapping(target = "authorities", expression = AUTHORITIES_MAPPER)
    User convertFromDTO(UserDTO userDTO);

    @Mapping(target = "authorities", expression = AUTHORITIES_MAPPER)
    User convertFromDTO(ManagedUserDTO userDTO);

    Project convertFromDTO(ProjectDTO dto);

    Notebook convertFromDTO(NotebookDTO dto);

    Experiment convertFromDTO(ExperimentDTO dto);

    Batch convertFromDTO(BatchDTO batchDTO);

    ComponentTemplate convertFromDTO(ComponentTemplateDTO componentTemplateDTO);

    ExperimentTemplate convertFromDTO(ExperimentTemplateDTO experimentTemplateDTO);
}
