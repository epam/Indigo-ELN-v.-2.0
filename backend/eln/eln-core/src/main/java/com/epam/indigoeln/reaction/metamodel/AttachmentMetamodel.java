package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;

public class AttachmentMetamodel {

    public static final Metamodel<AttachmentDTO, AttachmentPatch> INSTANCE = Metamodels.createMetamodel("Attachment", m -> {
        m.property("createdBy", AttachmentDTO::getCreatedBy, AttachmentDTO::setCreatedBy, AttachmentPatch::getCreatedBy, AttachmentPatch::setCreatedBy);
        m.property("createdAt", AttachmentDTO::getCreatedAt, AttachmentDTO::setCreatedAt, AttachmentPatch::getCreatedAt, AttachmentPatch::setCreatedAt);
        m.property("modifiedBy", AttachmentDTO::getModifiedBy, AttachmentDTO::setModifiedBy, AttachmentPatch::getModifiedBy, AttachmentPatch::setModifiedBy);
        m.property("modifiedAt", AttachmentDTO::getModifiedAt, AttachmentDTO::setModifiedAt, AttachmentPatch::getModifiedAt, AttachmentPatch::setModifiedAt);
        m.property("name", AttachmentDTO::getName, AttachmentDTO::setName, AttachmentPatch::getName, AttachmentPatch::setName);
        m.property("size", AttachmentDTO::getSize, AttachmentDTO::setSize, AttachmentPatch::getSize, AttachmentPatch::setSize);
    });
}
