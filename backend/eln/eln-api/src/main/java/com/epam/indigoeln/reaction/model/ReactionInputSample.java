package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Preconditions;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.appendToList;
import static com.epam.indigoeln.common.util.ModelUtil.removeFromList;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReactionInputSample extends ReactionSample<ReactionInput> {

    @NotNull
    private final InputSampleAnchor anchor;

    @Nullable
    private UUID sampleId;

    @Nullable
    private NbkBatchNumber nbkBatchNumber;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EnteredValue<MolUnit> mol = EnteredValue.empty();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EnteredValue<WeightUnit> weight = EnteredValue.empty();

    @Nullable
    private String comment;

    public static ReactionInputSample create(ReactionInput row, InputSampleAnchor anchor) {
        ReactionInputSample sample = new ReactionInputSample(anchor);
        sample.insertInto(row);
        return sample;
    }

    @Override
    public void insertInto(ReactionInput newParent) {
        //noinspection ConstantValue,DataFlowIssue
        Preconditions.checkState(row == null);
        row = newParent;
        row.setSamples(appendToList(row.getSamples(), this));
    }

    @Override
    public void delete() {
        //noinspection ConstantValue
        Preconditions.checkState(row != null);
        row.setSamples(removeFromList(row.getSamples(), this));
        //noinspection DataFlowIssue
        row = null;
    }
}
