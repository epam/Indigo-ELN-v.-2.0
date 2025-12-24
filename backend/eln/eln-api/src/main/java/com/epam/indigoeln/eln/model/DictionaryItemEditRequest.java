package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class DictionaryItemEditRequest {

    @Nullable
    private Optional<String> name;

    @Nullable
    private Optional<String> description;

    @Nullable
    private Optional<Integer> ordinal;

    @Nullable
    private Optional<Boolean> active;
}
