package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;

import java.util.Optional;
import java.util.UUID;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowSaltCode.class)
class SetInputRowSaltCodeHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowSaltCode> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowSaltCode mutation) {
        validate(!row.hasRealSamples(), "Cannot modify compound with linked samples");
        DictionaryItemRef oldSaltCode = row.getCompound().getSaltCode();
        //noinspection OptionalAssignedToNull
        row.setCompound(doUpdateCompound(row, Optional.ofNullable(mutation.saltCode()), null, null, null));
        return new MutationResult(formatSetterSummary("input compound salt code", mutation.saltCode())
                , new ReactionInputMutation.SetInputRowSaltCode(mutation.anchor(), oldSaltCode));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowSaltCode.class)
class SetOutputRowSaltCodeHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowSaltCode> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltCode mutation) {
        validate(!row.hasSamplesWithRegistrationStarted(), "Cannot modify compound when samples already sent for registration");
        DictionaryItemRef oldSaltCode = row.getCompound().getSaltCode();
        //noinspection OptionalAssignedToNull
        row.setCompound(doUpdateCompound(row, Optional.ofNullable(mutation.saltCode()), null, null, null));
        return new MutationResult(formatSetterSummary("output compound salt code", mutation.saltCode())
                , new ReactionOutputMutation.SetOutputRowSaltCode(mutation.anchor(), oldSaltCode)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputRowSaltEQ.class)
class SetInputRowSaltEQHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputRowSaltEQ> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputRowSaltEQ mutation) {
        validate(!row.hasRealSamples(), "Cannot modify compound with linked samples");
        Double oldValue = row.getCompound().getSaltEQ();
        //noinspection OptionalAssignedToNull
        row.setCompound(doUpdateCompound(row, null, Optional.ofNullable(mutation.saltEQ()), null, null));
        return new MutationResult(formatSetterSummary("input compound salt EQ", mutation.saltEQ())
                , new ReactionInputMutation.SetInputRowSaltEQ(mutation.anchor(), oldValue));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputRowSaltEQ.class)
class SetOutputRowSaltEQHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputRowSaltEQ> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltEQ mutation) {
        validate(!row.hasSamplesWithRegistrationStarted(), "Cannot modify compound when samples already sent for registration");
        Double oldValue = row.getCompound().getSaltEQ();
        //noinspection OptionalAssignedToNull
        row.setCompound(doUpdateCompound(row, null, Optional.ofNullable(mutation.saltEQ()), null, null));
        return new MutationResult(formatSetterSummary("output compound salt EQ", mutation.saltEQ())
                , new ReactionOutputMutation.SetOutputRowSaltEQ(mutation.anchor(), oldValue)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputCompoundStereoisomerCode.class)
class SetInputCompoundStereoisomerCodeHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputCompoundStereoisomerCode> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputCompoundStereoisomerCode mutation) {
        validate(!row.hasRealSamples(), "Cannot modify compound with linked samples");
        DictionaryItemRef oldStereoisomerCode = row.getCompound().getStereoisomerCode();
        //noinspection OptionalAssignedToNull
        row.setCompound(doUpdateCompound(row, null, null, Optional.ofNullable(mutation.stereoisomerCode()), null));
        return new MutationResult(formatSetterSummary("input compound stereoisomer code", mutation.stereoisomerCode())
                , new ReactionInputMutation.SetInputCompoundStereoisomerCode(mutation.anchor(), oldStereoisomerCode)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputCompoundStereoisomerCode.class)
class SetOutputCompoundStereoisomerCodeHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputCompoundStereoisomerCode> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundStereoisomerCode mutation) {
        validate(!row.hasSamplesWithRegistrationStarted(), "Cannot modify compound when samples already sent for registration");
        DictionaryItemRef oldStereoisomerCode = row.getCompound().getStereoisomerCode();
        //noinspection OptionalAssignedToNull
        row.setCompound(doUpdateCompound(row, null, null, Optional.ofNullable(mutation.stereoisomerCode()), null));
        return new MutationResult(formatSetterSummary("output compound stereoisomer code", mutation.stereoisomerCode())
                , new ReactionOutputMutation.SetOutputCompoundStereoisomerCode(mutation.anchor(), oldStereoisomerCode)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionInputMutation.SetInputCompoundMolWeight.class)
class SetInputCompoundMolWeightHandler extends AbstractReactionInputMutationHandler<ReactionInputMutation.SetInputCompoundMolWeight> {

    @Override
    public MutationResult doHandle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionInput row, ReactionInputMutation.SetInputCompoundMolWeight mutation) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            EnteredValueUndo<MolWeightUnit> undo = setEnteredValue(c::getMolWeight, c::setMolWeight, mutation.molWeight(), MolWeightUnit.G_PER_MOL, mutation.source(), experiment.getRevision());
            return new MutationResult(formatSetterSummary("input compound mol weight", mutation.molWeight(), MolWeightUnit.G_PER_MOL)
                , new ReactionInputMutation.SetInputCompoundMolWeight(mutation.anchor(), undo.value(), undo.source())
            );
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputMutation.SetOutputCompoundMolWeight.class)
class SetOutputCompoundMolWeightHandler extends AbstractReactionOutputMutationHandler<ReactionOutputMutation.SetOutputCompoundMolWeight> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundMolWeight mutation) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            EnteredValueUndo<MolWeightUnit> undo = setEnteredValue(c::getMolWeight, c::setMolWeight, mutation.molWeight(), MolWeightUnit.G_PER_MOL, mutation.source(), experiment.getRevision());
            return new MutationResult(formatSetterSummary("output compound mol weight", mutation.molWeight(), MolWeightUnit.G_PER_MOL)
                    , new ReactionOutputMutation.SetOutputCompoundMolWeight(mutation.anchor(), undo.value(), undo.source())
            );
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSaltCode.class)
class SetOutputSaltCodeHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSaltCode> {

    @Override
    protected ReactionOutputSampleMutation.SetOutputSaltCode doPrepareMutation(ExperimentEntity entity, ReactionOutputSampleMutation.SetOutputSaltCode mutation) {
        return new ReactionOutputSampleMutation.SetOutputSaltCode(mutation.anchor()
                , mutation.saltCode()
                , mutation.createdOutputAnchor() != null ? mutation.createdOutputAnchor() : new OutputAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSaltCode mutation) {
        validate(sample.getRegistrationStatus() == null, "Cannot modify compound for a sample already sent for registration");
        //noinspection OptionalAssignedToNull
        CompoundRef compound = doUpdateCompound(row, Optional.ofNullable(mutation.saltCode()), null, null, null);
        ReactionOutput newRow = findOrCreateOutputRow(reaction, compound, checkNotNull(mutation.createdOutputAnchor()));
        if (newRow != row) {
            sample.move(newRow, -1);
            cleanupUnintendedProducts(reaction);
        }
        return new MutationResult(formatSetterSummary("output sample salt code", mutation.saltCode())
                , null
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSaltEQ.class)
class SetOutputSaltEQHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSaltEQ> {

    @Override
    protected ReactionOutputSampleMutation.SetOutputSaltEQ doPrepareMutation(ExperimentEntity entity, ReactionOutputSampleMutation.SetOutputSaltEQ mutation) {
        return new ReactionOutputSampleMutation.SetOutputSaltEQ(mutation.anchor()
                , mutation.saltEQ()
                , mutation.createdOutputAnchor() != null ? mutation.createdOutputAnchor() : new OutputAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSaltEQ mutation) {
        validate(sample.getRegistrationStatus() == null, "Cannot modify compound for a sample already sent for registration");
        //noinspection OptionalAssignedToNull
        CompoundRef compound = doUpdateCompound(row, null, Optional.ofNullable(mutation.saltEQ()), null, null);
        ReactionOutput newRow = findOrCreateOutputRow(reaction, compound, checkNotNull(mutation.createdOutputAnchor()));
        if (newRow != row) {
            sample.move(newRow, -1);
            cleanupUnintendedProducts(reaction);
        }
        return new MutationResult(formatSetterSummary("output sample salt eq", mutation.saltEQ())
                , null
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStereoisomerCode.class)
class SetOutputStereoisomerCodeHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStereoisomerCode> {

    @Override
    protected ReactionOutputSampleMutation.SetOutputStereoisomerCode doPrepareMutation(ExperimentEntity entity, ReactionOutputSampleMutation.SetOutputStereoisomerCode mutation) {
        return new ReactionOutputSampleMutation.SetOutputStereoisomerCode(mutation.anchor()
                , mutation.stereoisomerCode()
                , mutation.createdOutputAnchor() != null ? mutation.createdOutputAnchor() : new OutputAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStereoisomerCode mutation) {
        validate(sample.getRegistrationStatus() == null, "Cannot modify compound for a sample already sent for registration");
        //noinspection OptionalAssignedToNull
        CompoundRef compound = doUpdateCompound(row, null, null, Optional.ofNullable(mutation.stereoisomerCode()), null);
        ReactionOutput newRow = findOrCreateOutputRow(reaction, compound, checkNotNull(mutation.createdOutputAnchor()));
        if (newRow != row) {
            sample.move(newRow, -1);
            cleanupUnintendedProducts(reaction);
        }
        return new MutationResult(formatSetterSummary("output sample stereoisomer code", mutation.stereoisomerCode())
                , null
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMolfile.class)
class SetOutputMolfileHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMolfile> {

    @Override
    protected ReactionOutputSampleMutation.SetOutputMolfile doPrepareMutation(ExperimentEntity entity, ReactionOutputSampleMutation.SetOutputMolfile mutation) {
        return new ReactionOutputSampleMutation.SetOutputMolfile(mutation.anchor()
                , mutation.molfile()
                , mutation.createdOutputAnchor() != null ? mutation.createdOutputAnchor() : new OutputAnchor(UUID.randomUUID())
        );
    }

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMolfile mutation) {
        validate(sample.getRegistrationStatus() == null, "Cannot modify compound for a sample already sent for registration");
        //noinspection OptionalAssignedToNull
        CompoundRef compound = doUpdateCompound(row, null, null, null, mutation.molfile());
        ReactionOutput newRow = findOrCreateOutputRow(reaction, compound, checkNotNull(mutation.createdOutputAnchor()));
        if (newRow != row) {
            sample.move(newRow, -1);
            cleanupUnintendedProducts(reaction);
        }
        return new MutationResult("Update output sample molfile"
                , null
        );
    }
}
