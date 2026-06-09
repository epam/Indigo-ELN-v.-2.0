package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

@Data
@With
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ExperimentEditRequest {

    @Nullable
    Optional<String> title;

    @Nullable
    Optional<TherapeuticAreaRef> therapeuticArea;

    @Nullable
    Optional<ProjectCodeRef> projectCode;

    @Nullable
    Optional<String> description;

    @Nullable
    Optional<String> literature;

    @Nullable
    Optional<Set<ExperimentRef>> linkedExperiments;

    @Nullable
    Optional<Set<ExperimentRef>> continuedFrom;

    @Nullable
    Optional<Set<ExperimentRef>> continuedTo;
}
