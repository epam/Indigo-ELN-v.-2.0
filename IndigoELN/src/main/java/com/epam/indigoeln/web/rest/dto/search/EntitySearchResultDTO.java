package com.epam.indigoeln.web.rest.dto.search;

import java.time.ZonedDateTime;

public class EntitySearchResultDTO {

    private String kind;

    private String name;

    private ZonedDateTime creationDate;

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

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
