package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.SetDiffHandler;

public class ProjectMetamodel {

    public static final Metamodel<ProjectSnapshot, ProjectPatch> INSTANCE = Metamodels.createMetamodel("Project", m -> {
        m.property("name", ProjectSnapshot::getName, ProjectSnapshot::setName, ProjectPatch::getName, ProjectPatch::setName);
        m.property("keywords", ProjectSnapshot::getKeywords, ProjectSnapshot::setKeywords, ProjectPatch::getKeywords, ProjectPatch::setKeywords);
        m.property("literature", ProjectSnapshot::getLiterature, ProjectSnapshot::setLiterature, ProjectPatch::getLiterature, ProjectPatch::setLiterature);
        m.property("description", ProjectSnapshot::getDescription, ProjectSnapshot::setDescription, ProjectPatch::getDescription, ProjectPatch::setDescription);
        m.property("attachments", ProjectSnapshot::getAttachments, ProjectSnapshot::setAttachments, ProjectPatch::getAttachments, ProjectPatch::setAttachments, new SetDiffHandler<>(AttachmentDTO::getId, new MetamodelDiffHandler<>(AttachmentMetamodel.INSTANCE, AttachmentPatch::new)));
        m.property("acl", ProjectSnapshot::getAcl, ProjectSnapshot::setAcl, ProjectPatch::getAcl, ProjectPatch::setAcl, new SetDiffHandler<>(ACLDetailsEntryDTO::getUsername, new MetamodelDiffHandler<>(ACLEntryMetamodel.INSTANCE, ACLEntryPatch::new)));
    });
}
