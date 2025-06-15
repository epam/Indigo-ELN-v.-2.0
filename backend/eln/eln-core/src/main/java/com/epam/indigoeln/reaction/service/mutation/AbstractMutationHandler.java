package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.fixed;

abstract class AbstractMutationHandler {

    protected CompoundRef.Stored realCompoundRef(CompoundEntity compound) {
        return new CompoundRef.Stored(compound.getId(), compound.getName(), fixed(compound.getMolWeight(), MolWeightUnit.G_PER_MOL), compound.getMolFile(), compound.getFormula());
    }

    protected CompoundRef.Virtual virtualCompoundRef(IndigoAPI.IndigoMolecule molecule) {
        CompoundRef.Virtual ref = new CompoundRef.Virtual(molecule.molfile(), molecule.grossFormula());
        ref.setMolWeight(fixed(molecule.molecularWeight(), MolWeightUnit.G_PER_MOL));
        return ref;
    }
}
