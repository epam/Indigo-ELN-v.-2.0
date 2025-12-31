package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.epam.indigoeln.reaction.model.patch.AttachmentPatch;

public class ACLEntryMetamodel {

    public static final Metamodel<ACLDetailsEntryDTO, ACLEntryPatch> INSTANCE = Metamodels.createMetamodel("ACLEntry", m -> {
        m.property("userId", ACLDetailsEntryDTO::getUserId, ACLDetailsEntryDTO::setUserId, ACLEntryPatch::getUserId, ACLEntryPatch::setUserId);
        m.property("displayName", ACLDetailsEntryDTO::getDisplayName, ACLDetailsEntryDTO::setDisplayName, ACLEntryPatch::getDisplayName, ACLEntryPatch::setDisplayName);
        m.property("level", ACLDetailsEntryDTO::getLevel, ACLDetailsEntryDTO::setLevel, ACLEntryPatch::getLevel, ACLEntryPatch::setLevel);
        m.property("inherited", ACLDetailsEntryDTO::getInherited, ACLDetailsEntryDTO::setInherited, ACLEntryPatch::getInherited, ACLEntryPatch::setInherited);
    });
}
