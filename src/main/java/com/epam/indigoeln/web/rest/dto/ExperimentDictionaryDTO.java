package com.epam.indigoeln.web.rest.dto;


import com.epam.indigoeln.core.model.Word;
import lombok.EqualsAndHashCode;

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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            ExperimentDictionaryItemDTO that = (ExperimentDictionaryItemDTO) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (notebookId != null ? !notebookId.equals(that.notebookId) : that.notebookId != null) return false;
            return projectId != null ? projectId.equals(that.projectId) : that.projectId == null;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + (notebookId != null ? notebookId.hashCode() : 0);
            result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
            return result;
        }
    }
}
