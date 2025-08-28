package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.SignatureTemplateEntity;
import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class SignatureTemplateMapper extends AbstractMapper {

    @IgnoreBaseFields
    @Mapping(target = "blocks", ignore = true)
    public abstract SignatureTemplateEntity requestToTemplate(SignatureTemplateRequest template);

    public abstract SignatureTemplateDTO entityToDTO(SignatureTemplateEntity entity);

    public abstract SignatureTemplateDetailsDTO entityToDetailsDTO(SignatureTemplateEntity entity);
}
