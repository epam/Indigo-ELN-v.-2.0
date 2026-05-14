package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class UserRequest {

    @NotEmpty
    private String username;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @Nullable
    private String password;

    @Nullable
    private List<RoleRef> roles;
}
