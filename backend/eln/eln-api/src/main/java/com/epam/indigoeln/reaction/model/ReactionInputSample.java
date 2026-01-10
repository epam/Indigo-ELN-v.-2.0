package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
@ToString(exclude = "row")
@EqualsAndHashCode(callSuper = true, exclude = "row")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionInputSample extends ReactionSample implements ExperimentNode {

    @JsonBackReference
    private ReactionInput row;

    @NotNull
    private InputSampleAnchor anchor;

    @Nullable
    private UUID sampleId;

    @Nullable
    private NbkBatchNumber nbkBatchNumber;

    @Nullable
    private EnteredValue<MolUnit> mol;

    @Nullable
    private EnteredValue<WeightUnit> weight;

    @Nullable
    private String comment;

    public static ReactionInputSample create(ReactionInput row, @Nullable InputSampleAnchor anchor) {
        ReactionInputSample sample = new ReactionInputSample();
        sample.row = row;
        sample.anchor = anchor != null ? anchor : row.getReaction().getModel().generateNextAnchor(InputSampleAnchor.class);
        return sample;
    }
}
