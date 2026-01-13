package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@Data
@With
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class NotebookEditRequest {

    @Nullable
    Optional<@NotEmpty String> name;

    @Nullable
    Optional<String> description;

    @AssertTrue(message = "Nothing to update")
    @JsonIgnore
    public boolean isNotEmpty() {
        //noinspection OptionalAssignedToNull
        return (name != null) || (description != null);
    }
}
