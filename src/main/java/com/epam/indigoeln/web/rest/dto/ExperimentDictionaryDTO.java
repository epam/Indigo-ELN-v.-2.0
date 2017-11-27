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
