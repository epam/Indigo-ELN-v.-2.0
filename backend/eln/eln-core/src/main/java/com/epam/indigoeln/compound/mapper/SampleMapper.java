package com.epam.indigoeln.compound.mapper;


import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SampleMapper {

    public abstract SampleDTO sampleToDTO(SampleEntity entity);
    public abstract List<SampleDTO> sampleToDTOList(List<SampleEntity> entities);
}
