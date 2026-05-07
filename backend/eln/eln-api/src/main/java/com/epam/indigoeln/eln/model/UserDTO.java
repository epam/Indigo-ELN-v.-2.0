package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.common.model.BaseDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserDTO extends BaseDTO {

    @NotEmpty
    String username;

    @NotEmpty
    String displayName;

    @NotNull
    Set<RoleRef> roles;

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", roles="  + roles +
                '}';
    }
}
