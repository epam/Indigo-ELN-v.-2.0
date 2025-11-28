package com.epam.indigoeln.reaction.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.CLASS )
@Target(ElementType.METHOD)
@Mapping(target = "mergeUnknownFields", ignore = true)
@Mapping(target = "unknownFields", ignore = true)
@Mapping(target = "mergeFrom", ignore = true)
@Mapping(target = "clearOneof", ignore = true)
@Mapping(target = "clearField", ignore = true)
@Mapping(target = "allFields", ignore = true)
public @interface IgnoreProtobufFields {
}
