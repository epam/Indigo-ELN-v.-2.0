package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.UserRef;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;

import java.time.ZonedDateTime;
import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class AttachmentMetamodel {

    public static final ModelProperty<AttachmentDTO, UserRef, AttachmentPatch, UserRef> CREATED_BY = property("createdBy",AttachmentDTO::getCreatedBy, AttachmentDTO::setCreatedBy, AttachmentPatch::getCreatedBy, AttachmentPatch::setCreatedBy);
    public static final ModelProperty<AttachmentDTO, ZonedDateTime, AttachmentPatch, ZonedDateTime> CREATED_AT = property("createdAt", AttachmentDTO::getCreatedAt, AttachmentDTO::setCreatedAt, AttachmentPatch::getCreatedAt, AttachmentPatch::setCreatedAt);
    public static final ModelProperty<AttachmentDTO, UserRef, AttachmentPatch, UserRef> MODIFIED_BY = property("modifiedBy", AttachmentDTO::getModifiedBy, AttachmentDTO::setModifiedBy, AttachmentPatch::getModifiedBy, AttachmentPatch::setModifiedBy);
    public static final ModelProperty<AttachmentDTO, ZonedDateTime, AttachmentPatch, ZonedDateTime> MODIFIED_AT = property("modifiedAt", AttachmentDTO::getModifiedAt, AttachmentDTO::setModifiedAt, AttachmentPatch::getModifiedAt, AttachmentPatch::setModifiedAt);
    public static final ModelProperty<AttachmentDTO, String, AttachmentPatch, String> NAME = property("name", AttachmentDTO::getName, AttachmentDTO::setName, AttachmentPatch::getName, AttachmentPatch::setName);
    public static final ModelProperty<AttachmentDTO, Long, AttachmentPatch, Long> SIZE = property("size", AttachmentDTO::getSize, AttachmentDTO::setSize, AttachmentPatch::getSize, AttachmentPatch::setSize);

    public static final Metamodel<AttachmentDTO, AttachmentPatch> INSTANCE = new Metamodel<>("Attachment", List.of(
            CREATED_BY,
            CREATED_AT,
            MODIFIED_BY,
            MODIFIED_AT,
            NAME,
            SIZE
    ));
}
