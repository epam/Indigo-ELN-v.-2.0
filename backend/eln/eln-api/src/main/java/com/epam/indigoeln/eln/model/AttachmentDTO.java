package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentDTO extends BaseDTO {

    public static void buildMetamodel(Metamodel<AttachmentDTO, AttachmentPatch> metamodel) {
        metamodel.setName("Attachment");
        metamodel.property("id", AttachmentDTO::getId, AttachmentDTO::setId, AttachmentPatch::getId, AttachmentPatch::setId);
        metamodel.property("createdBy", AttachmentDTO::getCreatedBy, AttachmentDTO::setCreatedBy, AttachmentPatch::getCreatedBy, AttachmentPatch::setCreatedBy);
        metamodel.property("createdAt", AttachmentDTO::getCreatedAt, AttachmentDTO::setCreatedAt, AttachmentPatch::getCreatedAt, AttachmentPatch::setCreatedAt);
        metamodel.property("modifiedBy", AttachmentDTO::getModifiedBy, AttachmentDTO::setModifiedBy, AttachmentPatch::getModifiedBy, AttachmentPatch::setModifiedBy);
        metamodel.property("modifiedAt", AttachmentDTO::getModifiedAt, AttachmentDTO::setModifiedAt, AttachmentPatch::getModifiedAt, AttachmentPatch::setModifiedAt);
        metamodel.property("name", AttachmentDTO::getName, AttachmentDTO::setName, AttachmentPatch::getName, AttachmentPatch::setName);
        metamodel.property("size", AttachmentDTO::getSize, AttachmentDTO::setSize, AttachmentPatch::getSize, AttachmentPatch::setSize);
    }

    @NotEmpty
    String name;

    @NotNull
    Long size;

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
