package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Data
@With
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
// null (absence of field in JSON) means don't update
// Optional.empty (field=null in JSON) means update to null
// Optional.of(value) means update to value
public class ProjectEditRequest {

    @Nullable
    Optional<@NotEmpty String> name;

    @Nullable
    Optional<List<@NotEmpty String>> keywords;

    @Nullable
    Optional<String> literature;

    @Nullable
    Optional<String> description;
}
