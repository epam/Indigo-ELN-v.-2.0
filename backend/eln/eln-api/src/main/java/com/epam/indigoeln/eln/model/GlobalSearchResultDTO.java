package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.reaction.model.ReactionRole;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
public class GlobalSearchResultDTO extends BaseDTO {

    private EntityType type;
    private String name;
    @Nullable
    private String fragment;
    @Nullable
    private Set<ReactionRole> reactionRoles;
    @Nullable
    private ExperimentStatus experimentStatus;
    @Nullable
    private Integer revision;
}
