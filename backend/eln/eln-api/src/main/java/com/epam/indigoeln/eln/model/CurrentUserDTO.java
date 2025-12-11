package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class CurrentUserDTO {

    UUID id;

    @NotEmpty
    String username;

    @NotEmpty
    String displayName;

    @NotNull
    Set<ApplicationPermission> permissions;

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
