package com.epam.indigoeln.web.rest.dto.search;

import java.time.ZonedDateTime;

public class EntitySearchResultDTO {

    private String kind;

    private String name;

    private Details details;

    private String projectId;

    private String notebookId;

    private String experimentId;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(String notebookId) {
        this.notebookId = notebookId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public static class Details {

        private ZonedDateTime creationDate;

        private String author;

        private String title;

        public ZonedDateTime getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(ZonedDateTime creationDate) {
            this.creationDate = creationDate;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
