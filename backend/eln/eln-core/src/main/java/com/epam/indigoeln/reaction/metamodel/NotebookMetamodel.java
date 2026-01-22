package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.SetDiffHandler;

public class NotebookMetamodel {

    public static final Metamodel<NotebookSnapshot, NotebookPatch> INSTANCE = Metamodels.createMetamodel("Notebook", m -> {
        m.property("name", NotebookSnapshot::getName, NotebookSnapshot::setName, NotebookPatch::getName, NotebookPatch::setName);
        m.property("description", NotebookSnapshot::getDescription, NotebookSnapshot::setDescription, NotebookPatch::getDescription, NotebookPatch::setDescription);
        m.property("attachments", NotebookSnapshot::getAttachments, NotebookSnapshot::setAttachments, NotebookPatch::getAttachments, NotebookPatch::setAttachments, new SetDiffHandler<>(AttachmentDTO::getId, new MetamodelDiffHandler<>(AttachmentMetamodel.INSTANCE, AttachmentPatch::new)));
        m.property("acl", NotebookSnapshot::getAcl, NotebookSnapshot::setAcl, NotebookPatch::getAcl, NotebookPatch::setAcl, new SetDiffHandler<>(ACLDetailsEntryDTO::getUsername, new MetamodelDiffHandler<>(ACLEntryMetamodel.INSTANCE, ACLEntryPatch::new)));
    });
}
