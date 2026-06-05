package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionInputSample extends ReactionSample<ReactionInput> {

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

    public static ReactionInputSample create(ReactionInput row, InputSampleAnchor anchor) {
        ReactionInputSample sample = new ReactionInputSample();
        sample.row = row;
        sample.anchor = anchor;
        row.getSamples().add(sample);
        return sample;
    }

    @Override
    protected List<? extends AbstractExperimentNode<ReactionInput>> internalGetSiblings(ReactionInput parent) {
        return parent.getSamples();
    }
}
