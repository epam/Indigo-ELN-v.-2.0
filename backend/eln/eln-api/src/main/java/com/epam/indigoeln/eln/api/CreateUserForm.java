package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.ApplicationRole;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public
class CreateUserForm {

    @NotEmpty
    private String username;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    private ApplicationRole @Nullable [] roles;
}
