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
package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Implementation of SectionModel interface for concept details.
 */
public class ConceptDetailsModel implements SectionModel {
    private ZonedDateTime creationDate;
    private String therapeuticArea;
    private List<String> linkedExperiment;
    private String projectCode;
    private String conceptKeywords;
    private List<String> designers;
    private List<String> coAuthors;

    public ConceptDetailsModel(ZonedDateTime creationDate, String therapeuticArea,
                               List<String> linkedExperiment, String projectCode,
                               String conceptKeywords, List<String> designers, List<String> coAuthors) {
        this.creationDate = creationDate;
        this.therapeuticArea = therapeuticArea;
        this.linkedExperiment = linkedExperiment;
        this.projectCode = projectCode;
        this.conceptKeywords = conceptKeywords;
        this.designers = designers;
        this.coAuthors = coAuthors;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public String getTherapeuticArea() {
        return therapeuticArea;
    }

    public List<String> getLinkedExperiment() {
        return linkedExperiment;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getConceptKeywords() {
        return conceptKeywords;
    }

    public List<String> getDesigners() {
        return designers;
    }

    public List<String> getCoAuthors() {
        return coAuthors;
    }
}
