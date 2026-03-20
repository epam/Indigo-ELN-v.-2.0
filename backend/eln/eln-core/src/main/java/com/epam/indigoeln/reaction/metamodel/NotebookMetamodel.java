package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.SetDiffHandler;

import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class NotebookMetamodel {

    public static final ModelProperty<NotebookSnapshot, String, NotebookPatch, String> NAME = property("name",NotebookSnapshot::getName, NotebookSnapshot::setName, NotebookPatch::getName, NotebookPatch::setName);
    public static final ModelProperty<NotebookSnapshot, String, NotebookPatch, String> DESCRIPTION = property("description", NotebookSnapshot::getDescription, NotebookSnapshot::setDescription, NotebookPatch::getDescription, NotebookPatch::setDescription);
    public static final ModelProperty<NotebookSnapshot, Set<AttachmentDTO>, NotebookPatch, ?> ATTACHMENTS = property("attachments", NotebookSnapshot::getAttachments, NotebookSnapshot::setAttachments, NotebookPatch::getAttachments, NotebookPatch::setAttachments, new SetDiffHandler<>(AttachmentDTO::getId, new MetamodelDiffHandler<>(AttachmentMetamodel.INSTANCE, AttachmentPatch::new)));
    public static final ModelProperty<NotebookSnapshot, Set<ACLDetailsEntryDTO>, NotebookPatch, ?> ACL = property("acl", NotebookSnapshot::getAcl, NotebookSnapshot::setAcl, NotebookPatch::getAcl, NotebookPatch::setAcl, new SetDiffHandler<>(ACLDetailsEntryDTO::getUsername, new MetamodelDiffHandler<>(ACLEntryMetamodel.INSTANCE, ACLEntryPatch::new)));

    public static final Metamodel<NotebookSnapshot, NotebookPatch> INSTANCE = new Metamodel<>("Notebook", List.of(
            NAME,
            DESCRIPTION,
            ATTACHMENTS,
            ACL
    ));
}
