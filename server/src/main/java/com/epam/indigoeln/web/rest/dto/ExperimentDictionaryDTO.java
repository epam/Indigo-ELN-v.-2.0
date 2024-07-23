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


import com.epam.indigoeln.core.model.Word;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;

public class ExperimentDictionaryDTO {

    private Set<ExperimentDictionaryItemDTO> words;

    ExperimentDictionaryDTO() {
        super();
    }

    public ExperimentDictionaryDTO(Set<ExperimentDictionaryItemDTO> words) {
        this.words = words;
    }

    public Set<ExperimentDictionaryItemDTO> getWords() {
        return words;
    }

    public void setWords(Set<ExperimentDictionaryItemDTO> words) {
        this.words = words;
    }

    public static class ExperimentDictionaryItemDTO extends Word {

        private String id;
        private String notebookId;
        private String projectId;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }

        public String getNotebookId() {
            return notebookId;
        }

        public void setNotebookId(String notebookId) {
            this.notebookId = notebookId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            ExperimentDictionaryItemDTO rhs = (ExperimentDictionaryItemDTO) obj;
            return new EqualsBuilder()
                    .appendSuper(super.equals(obj))
                    .append(this.id, rhs.id)
                    .append(this.notebookId, rhs.notebookId)
                    .append(this.projectId, rhs.projectId)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .appendSuper(super.hashCode())
                    .append(id)
                    .append(notebookId)
                    .append(projectId)
                    .toHashCode();
        }
    }
}
