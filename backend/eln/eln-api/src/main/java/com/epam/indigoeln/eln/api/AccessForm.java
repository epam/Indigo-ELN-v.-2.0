package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.AccessLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AccessForm {

    @NotNull
    private String username;

    @NotNull
    private AccessLevel level;

    private boolean deleteNested;

    public static List<AccessForm> of(String username, AccessLevel level) {
        return List.of(new AccessForm(username, level, false));
    }

    public static List<AccessForm> of(String username, AccessLevel level, boolean includeNested) {
        return List.of(new AccessForm(username, level, includeNested));
    }
}
