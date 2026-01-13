package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@Data
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ExperimentEditRequest {

    @Nullable
    Optional<DictionaryItemRef> therapeuticArea;

    @Nullable
    Optional<DictionaryItemRef> projectCode;

    @AssertTrue(message = "Nothing to update")
    @JsonIgnore
    public boolean isNotEmpty() {
        //noinspection OptionalAssignedToNull
        return (therapeuticArea != null) || (projectCode != null);
    }
}
