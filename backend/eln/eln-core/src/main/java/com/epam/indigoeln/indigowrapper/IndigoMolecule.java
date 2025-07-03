package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoMolecule extends AbstractIndigoObject {

    IndigoMolecule(IndigoSession session, IndigoObject obj) {
        super(session, obj);
    }

    public Iterable<IndigoAtom> atoms() {
        return new IndigoIterable<>(session, obj::iterateAtoms, o -> new IndigoAtom(session, o));
    }

    public String canonicalSmiles() {
        return obj.canonicalSmiles();
    }

    public String molfile() {
        return obj.molfile();
    }

    public String grossFormula() {
        return obj.grossFormula();
    }

    public double molecularWeight() {
        return obj.molecularWeight();
    }
}
