package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.patch.CompoundRefPatch;
import com.epam.indigoeln.reaction.util.Flag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.eln.util.PatchUtil.diff;

@RequiredArgsConstructor
public class CompoundRefDiffHandler extends AbstractDiffHandler<CompoundRef, CompoundRefPatch> {

    public static final CompoundRefDiffHandler INSTANCE = new CompoundRefDiffHandler();

    @Override
    @Nullable
    protected Patched<CompoundRef, CompoundRefPatch> doCompare(@Nullable CompoundRef a, CompoundRef b) {
        CompoundRefPatch patch = new CompoundRefPatch();
        Flag updated = new Flag();
        patch.setType(diff(updated, a, b, r -> switch (r) {
            case CompoundRef.Stored s -> CompoundRefPatch.Type.STORED;
            case CompoundRef.Virtual v -> CompoundRefPatch.Type.VIRTUAL;
            case CompoundRef.Unknown u -> CompoundRefPatch.Type.UNKNOWN;
        }));
        patch.setCompoundID(diff(updated, a, b, CompoundRef::getCompoundID));
        patch.setFormula(diff(updated, a, b, CompoundRef::getFormula));
        patch.setStereoisomerCode(diff(updated, a, b, CompoundRef::getStereoisomerCode));
        patch.setSaltCode(diff(updated, a, b, CompoundRef::getSaltCode));
        patch.setSaltEQ(diff(updated, a, b, CompoundRef::getSaltEQ, FloatDiffHandler.INSTANCE));
        patch.setCompoundKey(diff(updated, a, b, CompoundRef::getCompoundKey));
        patch.setMolWeight(diff(updated, a, b, CompoundRef::getMolWeight, EnteredValueDiffHandler.instance()));
        patch.setExactMass(diff(updated, a, b, CompoundRef::getExactMass, FloatDiffHandler.INSTANCE));
        patch.setCasNumber(diff(updated, a, b, CompoundRef::getCasNumber));
        patch.setCalculatedBatchMF(diff(updated, a, b, CompoundRef::getCalculatedBatchMF));
        return updated.isSet() ? Patched.updated(patch) : null;
    }
}
