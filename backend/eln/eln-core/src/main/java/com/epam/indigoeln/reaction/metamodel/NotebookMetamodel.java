package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;

import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class NotebookMetamodel {

    public static final ModelProperty<NotebookSnapshot, String> NAME = property("name",NotebookSnapshot::getName, NotebookSnapshot::setName);
    public static final ModelProperty<NotebookSnapshot, String> DESCRIPTION = property("description", NotebookSnapshot::getDescription, NotebookSnapshot::setDescription);
    public static final ModelProperty<NotebookSnapshot, Set<AttachmentDTO>> ATTACHMENTS = property("attachments", NotebookSnapshot::getAttachments, NotebookSnapshot::setAttachments);
    public static final ModelProperty<NotebookSnapshot, Set<ACLDetailsEntryDTO>> ACL = property("acl", NotebookSnapshot::getAcl, NotebookSnapshot::setAcl);

    public static final Metamodel<NotebookSnapshot> INSTANCE = new Metamodel<>("Notebook", List.of(
            NAME,
            DESCRIPTION,
            ATTACHMENTS,
            ACL
    ));
}
