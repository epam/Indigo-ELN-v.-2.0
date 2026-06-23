package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class DictionaryItemEditRequest {

    private JsonNullable<String> name = JsonNullable.undefined();

    private JsonNullable<String> description = JsonNullable.undefined();

    private JsonNullable<Integer> ordinal = JsonNullable.undefined();

    private JsonNullable<Boolean> active = JsonNullable.undefined();
}
