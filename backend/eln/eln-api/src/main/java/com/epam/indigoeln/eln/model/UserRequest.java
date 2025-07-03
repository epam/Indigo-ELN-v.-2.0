package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class UserRequest {

    @NotEmpty
    private String username;

    @Nullable
    private String displayName;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String password;

    private ApplicationRole @Nullable [] roles;
}
