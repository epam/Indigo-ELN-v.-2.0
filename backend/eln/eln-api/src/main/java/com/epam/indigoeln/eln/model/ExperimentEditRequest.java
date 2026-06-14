package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Data
@With
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ExperimentEditRequest {

    JsonNullable<String> title = JsonNullable.undefined();

    JsonNullable<TherapeuticAreaRef> therapeuticArea = JsonNullable.undefined();

    JsonNullable<ProjectCodeRef> projectCode = JsonNullable.undefined();

    JsonNullable<String> description = JsonNullable.undefined();

    JsonNullable<String> literature = JsonNullable.undefined();

    JsonNullable<Set<ExperimentRef>> linkedExperiments = JsonNullable.undefined();

    JsonNullable<Set<ExperimentRef>> continuedFrom = JsonNullable.undefined();

    JsonNullable<Set<ExperimentRef>> continuedTo = JsonNullable.undefined();
}
