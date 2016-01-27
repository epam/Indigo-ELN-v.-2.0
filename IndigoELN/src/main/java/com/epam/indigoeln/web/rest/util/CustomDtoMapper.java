package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.Authority;
import com.epam.indigoeln.core.model.Batch;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Template;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.web.rest.dto.BatchDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import com.epam.indigoeln.web.rest.dto.TemplateDTO;
import com.epam.indigoeln.web.rest.dto.ManagedUserDTO;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import com.epam.indigoeln.web.rest.dto.UserDTO;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom MapStruct mapper for converting DTO/Model objects
 */
@Mapper
public interface CustomDtoMapper {

    @Mapping(target = "authorities", expression = "java(convertAuthorities(userDTO.getAuthorities()))")
    User convertFromDTO(UserDTO userDTO);

    @Mapping(target = "authorities", expression = "java(convertAuthorities(userDTO.getAuthorities()))")
    User convertFromDTO(ManagedUserDTO userDTO);

    Project convertFromDTO(ProjectDTO dto);

    ProjectDTO convertToDTO(Project project);

    Notebook convertFromDTO(NotebookDTO dto);

    NotebookDTO convertToDTO(Notebook notebook);

    Experiment convertFromDTO(ExperimentDTO dto);

    ExperimentDTO convertToDTO(Experiment experiment);

    Batch convertFromDTO(BatchDTO batchDTO);

    @Mapping(target = "templateContent", expression = "java(convertJsonToDbObject(templateDTO.getTemplateContent()))")
    Template convertFromDTO(TemplateDTO templateDTO);


    default BasicDBObject convertJsonToDbObject(JSONObject json) {
        return json != null ? (BasicDBObject) JSON.parse(json.toString()) : null;
    }

    default Set<Authority> convertAuthorities(Set<String> authorities) {
        return authorities.stream().map(s -> new Authority(s)).collect(Collectors.toSet());
    }
}
