package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
@With
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class NotebookEditRequest {

    JsonNullable<String> name = JsonNullable.undefined();

    JsonNullable<String> description = JsonNullable.undefined();
}
