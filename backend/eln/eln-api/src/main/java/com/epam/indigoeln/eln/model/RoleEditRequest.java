package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Data
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class RoleEditRequest {

    JsonNullable<String> name = JsonNullable.undefined();

    JsonNullable<Set<ApplicationPermission>> permissions = JsonNullable.undefined();
}
