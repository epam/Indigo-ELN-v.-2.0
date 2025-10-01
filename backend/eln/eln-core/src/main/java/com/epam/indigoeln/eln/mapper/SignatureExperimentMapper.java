package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.ExperimentForSignatureDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class SignatureExperimentMapper extends AbstractMapper {

    public abstract ExperimentForSignatureDTO entityToDTO(ExperimentEntity experiment);
    public abstract List<ExperimentForSignatureDTO> entityToDTOList(List<ExperimentEntity> experiments);
}
