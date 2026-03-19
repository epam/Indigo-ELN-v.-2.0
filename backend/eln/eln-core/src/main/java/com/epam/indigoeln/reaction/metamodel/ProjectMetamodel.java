package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.SetDiffHandler;

import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ProjectMetamodel {

    public static final ModelProperty<ProjectSnapshot, String, ProjectPatch, String> NAME = property("name",ProjectSnapshot::getName, ProjectSnapshot::setName, ProjectPatch::getName, ProjectPatch::setName);
    public static final ModelProperty<ProjectSnapshot, Set<String>, ProjectPatch, Set<String>> KEYWORDS = property("keywords", ProjectSnapshot::getKeywords, ProjectSnapshot::setKeywords, ProjectPatch::getKeywords, ProjectPatch::setKeywords);
    public static final ModelProperty<ProjectSnapshot, String, ProjectPatch, String> LITERATURE = property("literature", ProjectSnapshot::getLiterature, ProjectSnapshot::setLiterature, ProjectPatch::getLiterature, ProjectPatch::setLiterature);
    public static final ModelProperty<ProjectSnapshot, String, ProjectPatch, String> DESCRIPTION = property("description", ProjectSnapshot::getDescription, ProjectSnapshot::setDescription, ProjectPatch::getDescription, ProjectPatch::setDescription);
    public static final ModelProperty<ProjectSnapshot, Set<AttachmentDTO>, ProjectPatch, ?> ATTACHMENTS = property("attachments", ProjectSnapshot::getAttachments, ProjectSnapshot::setAttachments, ProjectPatch::getAttachments, ProjectPatch::setAttachments, new SetDiffHandler<>(AttachmentDTO::getId, new MetamodelDiffHandler<>(AttachmentMetamodel.INSTANCE, AttachmentPatch::new)));
    public static final ModelProperty<ProjectSnapshot, Set<ACLDetailsEntryDTO>, ProjectPatch, ?> ACL = property("acl", ProjectSnapshot::getAcl, ProjectSnapshot::setAcl, ProjectPatch::getAcl, ProjectPatch::setAcl, new SetDiffHandler<>(ACLDetailsEntryDTO::getUsername, new MetamodelDiffHandler<>(ACLEntryMetamodel.INSTANCE, ACLEntryPatch::new)));

    public static final Metamodel<ProjectSnapshot, ProjectPatch> INSTANCE = new Metamodel<>("Project", List.of(
            NAME,
            KEYWORDS,
            LITERATURE,
            DESCRIPTION,
            ATTACHMENTS,
            ACL
    ));
}
