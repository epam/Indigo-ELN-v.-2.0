package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;

import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ProjectMetamodel {

    public static final ModelProperty<ProjectSnapshot, String> NAME = property("name",ProjectSnapshot::getName, ProjectSnapshot::setName);
    public static final ModelProperty<ProjectSnapshot, Set<String>> KEYWORDS = property("keywords", ProjectSnapshot::getKeywords, ProjectSnapshot::setKeywords);
    public static final ModelProperty<ProjectSnapshot, String> LITERATURE = property("literature", ProjectSnapshot::getLiterature, ProjectSnapshot::setLiterature);
    public static final ModelProperty<ProjectSnapshot, String> DESCRIPTION = property("description", ProjectSnapshot::getDescription, ProjectSnapshot::setDescription);
    public static final ModelProperty<ProjectSnapshot, Set<AttachmentDTO>> ATTACHMENTS = property("attachments", ProjectSnapshot::getAttachments, ProjectSnapshot::setAttachments);
    public static final ModelProperty<ProjectSnapshot, Set<ACLEntryDTO>> ACL = property("acl", ProjectSnapshot::getAcl, ProjectSnapshot::setAcl);

    public static final Metamodel<ProjectSnapshot> INSTANCE = new Metamodel<>("Project", List.of(
            NAME,
            KEYWORDS,
            LITERATURE,
            DESCRIPTION,
            ATTACHMENTS,
            ACL
    ));
}
