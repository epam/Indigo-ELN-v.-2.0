package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ACLEntryMetamodel {

    public static final ModelProperty<ACLDetailsEntryDTO, UUID, ACLEntryPatch, UUID> USER_ID = property("userId", ACLDetailsEntryDTO::getUserId, ACLDetailsEntryDTO::setUserId, ACLEntryPatch::getUserId, ACLEntryPatch::setUserId);
    public static final ModelProperty<ACLDetailsEntryDTO, String, ACLEntryPatch, String> DISPLAY_NAME = property("displayName", ACLDetailsEntryDTO::getDisplayName, ACLDetailsEntryDTO::setDisplayName, ACLEntryPatch::getDisplayName, ACLEntryPatch::setDisplayName);
    public static final ModelProperty<ACLDetailsEntryDTO, AccessLevel, ACLEntryPatch, AccessLevel> LEVEL = property("level", ACLDetailsEntryDTO::getLevel, ACLDetailsEntryDTO::setLevel, ACLEntryPatch::getLevel, ACLEntryPatch::setLevel);
    public static final ModelProperty<ACLDetailsEntryDTO, Boolean, ACLEntryPatch, Boolean> INHERITED = property("inherited", ACLDetailsEntryDTO::getInherited, ACLDetailsEntryDTO::setInherited, ACLEntryPatch::getInherited, ACLEntryPatch::setInherited);

    public static final Metamodel<ACLDetailsEntryDTO, ACLEntryPatch> INSTANCE = new Metamodel<>("ACLEntry", List.of(
            USER_ID,
            DISPLAY_NAME,
            LEVEL,
            INHERITED
    ));
}
