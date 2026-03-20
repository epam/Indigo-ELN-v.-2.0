package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ACLEntryMetamodel {

    public static final ModelProperty<ACLDetailsEntryDTO, UUID> USER_ID = property("userId", ACLDetailsEntryDTO::getUserId, ACLDetailsEntryDTO::setUserId);
    public static final ModelProperty<ACLDetailsEntryDTO, String> DISPLAY_NAME = property("displayName", ACLDetailsEntryDTO::getDisplayName, ACLDetailsEntryDTO::setDisplayName);
    public static final ModelProperty<ACLDetailsEntryDTO, AccessLevel> LEVEL = property("level", ACLDetailsEntryDTO::getLevel, ACLDetailsEntryDTO::setLevel);
    public static final ModelProperty<ACLDetailsEntryDTO, Boolean> INHERITED = property("inherited", ACLDetailsEntryDTO::getInherited, ACLDetailsEntryDTO::setInherited);

    public static final Metamodel<ACLDetailsEntryDTO> INSTANCE = new Metamodel<>("ACLEntry", List.of(
            USER_ID,
            DISPLAY_NAME,
            LEVEL,
            INHERITED
    ));
}
