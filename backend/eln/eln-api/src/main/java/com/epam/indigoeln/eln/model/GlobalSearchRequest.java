package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.compound.model.search.NumericSearch;
import com.epam.indigoeln.compound.model.search.StructuralSearch;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class GlobalSearchRequest {

    @Nullable
    String query;

    @Nullable
    TherapeuticAreaRef therapeuticArea;

    @Nullable
    ProjectCodeRef projectCode;

    @Nullable
    Set<ExperimentStatus> experimentStatus;

    @Nullable
    Set<UserRef> author;

    @Nullable
    NumericSearch batchYield;

    @Nullable
    NumericSearch batchPurity;

    @Nullable
    StructuralSearch moleculeStructure;

    @Nullable
    ReactionRole reactionRole;

    @Nullable
    StructuralSearch reactionStructure;

    @JsonIgnore
    public boolean isEmpty() {
        return query == null
                && therapeuticArea == null
                && projectCode == null
                && experimentStatus == null
                && author == null
                && batchYield == null
                && batchPurity == null
                && moleculeStructure == null
                && reactionStructure == null;
    }

    @AssertTrue(message = "reactionRole requires moleculeStructure to be set")
    @JsonIgnore
    public boolean isReactionRoleValid() {
        return reactionRole == null || moleculeStructure != null;
    }
}
