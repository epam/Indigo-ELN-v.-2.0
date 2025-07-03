package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoReaction extends AbstractIndigoObject {

    IndigoReaction(IndigoSession session, IndigoObject obj) {
        super(session, obj);
    }

    public Iterable<IndigoMolecule> reactants() {
        return new IndigoIterable<>(session, obj::iterateReactants, o -> new IndigoMolecule(session, o));
    }

    public Iterable<IndigoMolecule> catalysts() {
        return new IndigoIterable<>(session, obj::iterateCatalysts, o -> new IndigoMolecule(session, o));
    }

    public Iterable<IndigoMolecule> products() {
        return new IndigoIterable<>(session, obj::iterateProducts, o -> new IndigoMolecule(session, o));
    }
}
