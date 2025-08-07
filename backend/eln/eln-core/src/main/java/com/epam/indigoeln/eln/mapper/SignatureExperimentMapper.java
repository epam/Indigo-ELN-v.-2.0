package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.SignatureTemplateEntity;
import com.epam.indigoeln.eln.model.ExperimentForSignatureDTO;
import com.epam.indigoeln.eln.model.SignatureTemplateDTO;
import com.epam.indigoeln.eln.model.SignatureTemplateDetailsDTO;
import com.epam.indigoeln.eln.model.SignatureTemplateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class SignatureExperimentMapper extends AbstractMapper {

    public abstract ExperimentForSignatureDTO entityToDTO(ExperimentEntity experiment);
}
