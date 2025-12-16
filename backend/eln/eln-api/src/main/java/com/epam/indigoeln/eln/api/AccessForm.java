package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.AccessLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AccessForm {

    @NotNull
    private UUID userID;

    @NotNull
    private AccessLevel level;

    public static List<AccessForm> of(UUID userID, AccessLevel level) {
        return List.of(new AccessForm(userID, level));
    }
}
