package com.epam.indigoeln.web.rest.dto.search;

import com.epam.indigoeln.core.model.User;

import java.time.ZonedDateTime;

public class EntitySearchResultDTO {

    private String kind;

    private String name;

    private Details details;

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
