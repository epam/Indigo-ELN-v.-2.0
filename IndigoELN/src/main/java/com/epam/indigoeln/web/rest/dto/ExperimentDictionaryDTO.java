package com.epam.indigoeln.web.rest.dto;


import com.epam.indigoeln.core.model.Word;

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

        public String getId() {
            return id;
        }

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
    }
}
