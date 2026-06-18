package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;

import java.time.Instant;
import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class AttachmentMetamodel {

    public static final ModelProperty<AttachmentDTO, UserRef> CREATED_BY = property("createdBy",AttachmentDTO::getCreatedBy, AttachmentDTO::setCreatedBy);
    public static final ModelProperty<AttachmentDTO, Instant> CREATED_AT = property("createdAt", AttachmentDTO::getCreatedAt, AttachmentDTO::setCreatedAt);
    public static final ModelProperty<AttachmentDTO, UserRef> MODIFIED_BY = property("modifiedBy", AttachmentDTO::getModifiedBy, AttachmentDTO::setModifiedBy);
    public static final ModelProperty<AttachmentDTO, Instant> MODIFIED_AT = property("modifiedAt", AttachmentDTO::getModifiedAt, AttachmentDTO::setModifiedAt);
    public static final ModelProperty<AttachmentDTO, String> NAME = property("name", AttachmentDTO::getName, AttachmentDTO::setName);
    public static final ModelProperty<AttachmentDTO, Long> SIZE = property("size", AttachmentDTO::getSize, AttachmentDTO::setSize);

    public static final Metamodel<AttachmentDTO> INSTANCE = new Metamodel<>("Attachment", List.of(
            CREATED_BY,
            CREATED_AT,
            MODIFIED_BY,
            MODIFIED_AT,
            NAME,
            SIZE
    ));
}
