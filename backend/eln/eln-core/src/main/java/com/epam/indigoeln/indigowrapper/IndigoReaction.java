package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

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
}
