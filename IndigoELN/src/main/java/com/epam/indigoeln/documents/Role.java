package com.epam.indigoeln.documents;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="role")
public class Role {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
