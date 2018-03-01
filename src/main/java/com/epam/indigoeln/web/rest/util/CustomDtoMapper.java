/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.web.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Custom MapStruct mapper for converting DTO/Model objects.
 */
@Mapper
public interface CustomDtoMapper {

    String ACCESS_LIST_MAPPER = "java(dto.getAccessList().stream()."
            + "map(s -> new com.epam.indigoeln.core.model.UserPermission(s.getUser(), s.getPermissions()))."
            + "collect(java.util.stream.Collectors.toSet()))";

    @Mapping(target = "authorities", ignore = true)
    User convertFromDTO(UserDTO userDTO);

    @Mapping(target = "authorities", ignore = true)
    User convertFromDTO(ManagedUserDTO userDTO);

    RoleDTO convertToDTO(Role role);

    Role convertFromDTO(RoleDTO roleDTO);

    Component convertFromDTO(ComponentDTO componentDTO);

    @Mapping(target = "accessList", ignore = true)
    Dictionary convertFromDTO(DictionaryDTO dictionaryDTO);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    @Mapping(target = "template", ignore = true)
    Experiment convertFromDTO(ExperimentDTO dto);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    Template convertFromDTO(TemplateDTO dto);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    Project convertFromDTO(ProjectDTO dto);

    @Mapping(target = "accessList", expression = ACCESS_LIST_MAPPER)
    Notebook convertFromDTO(NotebookDTO dto);
}
