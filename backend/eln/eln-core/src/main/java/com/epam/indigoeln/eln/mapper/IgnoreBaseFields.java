package com.epam.indigoeln.eln.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.CLASS )
@Target(ElementType.METHOD)
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "modifiedBy", ignore = true)
@Mapping(target = "modifiedAt", ignore = true)
public @interface IgnoreBaseFields {
}
