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

    String AUTHORITIES_MAPPER = "java(userDTO.getAuthorities().stream()." +
                                "map(s -> new com.epam.indigoeln.core.model.Authority(s))." +
                                "collect(java.util.stream.Collectors.toSet()))";

    @Mapping(target = "authorities", expression = AUTHORITIES_MAPPER)
    User convertFromDTO(UserDTO userDTO);

    @Mapping(target = "authorities", expression = AUTHORITIES_MAPPER)
    User convertFromDTO(ManagedUserDTO userDTO);

    Project convertFromDTO(ProjectDTO dto);

    ProjectDTO convertToDTO(Project project);

    Notebook convertFromDTO(NotebookDTO dto);

    NotebookDTO convertToDTO(Notebook notebook);

    Experiment convertFromDTO(ExperimentDTO dto);

    ExperimentDTO convertToDTO(Experiment experiment);

    Batch convertFromDTO(BatchDTO batchDTO);
}
