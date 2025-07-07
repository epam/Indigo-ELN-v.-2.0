package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;
import one.util.streamex.StreamEx;

public class IndigoReaction extends AbstractIndigoObject {

    IndigoReaction(IndigoSession indigo, IndigoObject obj) {
        super(indigo, obj);
    }

    public Iterable<IndigoMolecule> reactants() {
        return new IndigoIterable<>(obj::iterateReactants, o -> new IndigoMolecule(session, o));
    }

    public Iterable<IndigoMolecule> catalysts() {
        return new IndigoIterable<>(obj::iterateCatalysts, o -> new IndigoMolecule(session, o));
    }

    public Iterable<IndigoMolecule> products() {
        return new IndigoIterable<>(obj::iterateProducts, o -> new IndigoMolecule(session, o));
    }

    public void addReactant(IndigoMolecule molecule) {
        obj.addReactant(molecule.obj);
    }

    public void addCatalyst(IndigoMolecule molecule) {
        obj.addCatalyst(molecule.obj);
    }

    public void addProduct(IndigoMolecule molecule) {
        obj.addProduct(molecule.obj);
    }

    public String rxnfile() {
        return obj.rxnfile();
    }

    @Override
    public String toString() {
        return StreamEx.of(reactants().iterator()).joining(" + ") + " -> " + StreamEx.of(products().iterator()).joining(" + ");
    }
}
