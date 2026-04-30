package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DictionaryItemRef {

    @NotNull
    private final UUID id;

    @NotEmpty
    private final String name;

    @JsonIgnore
    private final boolean active;

    @JsonIgnore
    private final boolean deleted;

    @JsonIgnore
    private final UUID dictionaryID;

    @Override
    public String toString() {
        return name;
    }

    @JsonIgnore
    public boolean isInactive() {
        return !active || deleted;
    }

    @Nullable
    @JsonIgnore
    public BuiltInDictionary getBuildInDictionary() {
        return BuiltInDictionary.lookup(dictionaryID);
    }

    public interface Creator {

        DictionaryItemRef create(UUID id, String name, boolean active, boolean deleted, UUID dictionaryID);
    }
}
