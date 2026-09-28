package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.common.model.BaseDTO;
import com.epam.indigoeln.reaction.model.ReactionRole;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
public class GlobalSearchResultDTO extends BaseDTO {

    private ELNEntityType type;
    /** A code, not a title: "00000001-0001" for an experiment, "00000001" for a notebook. */
    private String name;
    /** The human-readable subject. Experiments only. */
    @Nullable
    private String title;
    @Nullable
    private String fragment;
    @Nullable
    private Set<ReactionRole> reactionRoles;
    @Nullable
    private ExperimentStatus experimentStatus;
    @Nullable
    private Integer revision;
    /** Notebooks in this project. Projects only. */
    @Nullable
    private Integer notebookCount;
    /** Experiments in this project or notebook. Projects and notebooks only. */
    @Nullable
    private Integer experimentCount;
}
