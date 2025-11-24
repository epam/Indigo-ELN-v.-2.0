package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.SignatureTemplateEntity;
import com.epam.indigoeln.eln.model.SignatureTemplateDTO;
import com.epam.indigoeln.eln.model.SignatureTemplateDetailsDTO;
import com.epam.indigoeln.eln.model.SignatureTemplateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class SignatureTemplateMapper extends AbstractMapper {

    @IgnoreBaseFields
    @Mapping(target = "blocks", ignore = true)
    public abstract SignatureTemplateEntity requestToTemplate(SignatureTemplateRequest template);

    public abstract SignatureTemplateDTO entityToDTO(SignatureTemplateEntity entity);

    public abstract SignatureTemplateDetailsDTO entityToDetailsDTO(SignatureTemplateEntity entity);
}
