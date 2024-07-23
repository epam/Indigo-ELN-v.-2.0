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
package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Notebook;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * DTO for Notebook.
 */
@JsonTypeName("Notebook")
public class NotebookDTO extends BasicDTO {

    private String description;
    private List<ExperimentDTO> experiments;

    public NotebookDTO() {
        super();
    }

    public NotebookDTO(Notebook notebook) {
        super(notebook);
        this.description = notebook.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ExperimentDTO> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<ExperimentDTO> experiments) {
        this.experiments = experiments;
    }

    @Override
    public String toString() {
        return "NotebookDTO{} " + super.toString();
    }
}
