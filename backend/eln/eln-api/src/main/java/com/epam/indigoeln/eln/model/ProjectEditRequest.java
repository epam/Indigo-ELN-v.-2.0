package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Data
@With
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ProjectEditRequest {

    JsonNullable<String> name = JsonNullable.undefined();

    JsonNullable<List<String>> keywords = JsonNullable.undefined();

    JsonNullable<String> literature = JsonNullable.undefined();

    JsonNullable<String> description = JsonNullable.undefined();
}
