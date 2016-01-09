package com.epam.indigoeln.core.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="notebook")
public class Notebook {
    @Id
    private String id;
    private String name;

    public Notebook() {
    }

    public Notebook(String name) {
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notebook)) return false;

        Notebook notebook = (Notebook) o;

        if (id != null ? !id.equals(notebook.id) : notebook.id != null) return false;
        if (name != null ? !name.equals(notebook.name) : notebook.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
