package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
public class ACLEntryDTO {

    public static void buildMetamodel(Metamodel<ACLEntryDTO, ACLEntryPatch> metamodel) {
        metamodel.setName("ACLEntry");
        metamodel.property("userId", ACLEntryDTO::getUserId, ACLEntryDTO::setUserId, ACLEntryPatch::getUserId, ACLEntryPatch::setUserId);
        metamodel.property("displayName", ACLEntryDTO::getDisplayName, ACLEntryDTO::setDisplayName, ACLEntryPatch::getDisplayName, ACLEntryPatch::setDisplayName);
        metamodel.property("level", ACLEntryDTO::getLevel, ACLEntryDTO::setLevel, ACLEntryPatch::getLevel, ACLEntryPatch::setLevel);
        metamodel.property("inherited", ACLEntryDTO::getInherited, ACLEntryDTO::setInherited, ACLEntryPatch::getInherited, ACLEntryPatch::setInherited);
    }

    @NotNull
    private UUID userId;

    @NotNull
    private String displayName;

    @NotNull
    private AccessLevel level;

    @NotNull
    private Boolean inherited;
}
