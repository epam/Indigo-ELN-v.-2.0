package com.epam.indigoeln.core.model;


import com.epam.indigoeln.core.security.AuthoritiesConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * An authority (a security role) used by Spring Security.
 */
@Document(collection = "authority")
public class Authority implements Serializable {

    public static final Authority ADMIN = new Authority(AuthoritiesConstants.ADMIN);
    public static final Authority USER = new Authority(AuthoritiesConstants.USER);
    public static final Authority VIEWER = new Authority(AuthoritiesConstants.VIEWER);

    @NotNull
    @Size(min = 0, max = 50)
    @Id
    private String name;

    public Authority() {

    }

    public Authority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Authority authority = (Authority) o;

        if (name != null ? !name.equals(authority.name) : authority.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Authority{" +
                "name='" + name + '\'' +
                "}";
    }
}
