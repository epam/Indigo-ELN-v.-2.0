package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class AttachmentMapper extends AbstractMapper {

    public abstract AttachmentDTO attachmentToDTO(AttachmentEntity record);
    public abstract List<AttachmentDTO> attachmentToDTOList(Collection<AttachmentEntity> entities);
}
