package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserDTO extends BaseDTO {

    @NotEmpty
    String name;

    @NotNull
    Integer aclCount;

    @NotEmpty
    String username;

    @NotEmpty
    String firstName;

    @NotEmpty
    String lastName;

    @NotEmpty
    String displayName;

    @NotEmpty
    ApplicationRole[] roles;

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
