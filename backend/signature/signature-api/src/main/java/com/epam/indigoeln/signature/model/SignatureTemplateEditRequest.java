package com.epam.indigoeln.signature.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Data
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class SignatureTemplateEditRequest {

    JsonNullable<String> name = JsonNullable.undefined();

    JsonNullable<List<SignatureTemplateBlock>> blocks = JsonNullable.undefined();
}
