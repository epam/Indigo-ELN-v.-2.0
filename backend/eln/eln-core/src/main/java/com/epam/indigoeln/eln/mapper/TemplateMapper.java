package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.model.TemplateDTO;
import com.epam.indigoeln.eln.model.TemplateDetailsDTO;
import com.epam.indigoeln.eln.model.TemplateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class TemplateMapper extends AbstractMapper {

    @IgnoreBaseFields
    public abstract TemplateEntity requestToTemplate(TemplateRequest template);

    public abstract TemplateDTO entityToDTO(TemplateEntity entity);

    public abstract TemplateDetailsDTO entityToDetailsDTO(TemplateEntity entity);
}
