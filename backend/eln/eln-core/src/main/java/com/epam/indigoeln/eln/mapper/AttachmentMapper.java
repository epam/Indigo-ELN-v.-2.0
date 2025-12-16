package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class AttachmentMapper extends AbstractMapper {

    @IgnoreBaseFields
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "notebooks", ignore = true)
    @Mapping(target = "experiments", ignore = true)
    @Mapping(target = "size", expression = "java((long) content.length)")
    public abstract AttachmentEntity requestToAttachment(String name, byte[] content);

    public abstract AttachmentDTO attachmentToDTO(AttachmentEntity record);
    public abstract List<AttachmentDTO> attachmentToDTOList(Collection<AttachmentEntity> entities);
}
