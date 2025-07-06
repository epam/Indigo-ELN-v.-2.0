package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoMolecule extends AbstractIndigoObject {

    IndigoMolecule(IndigoSession indigo, IndigoObject obj) {
        super(indigo, obj);
    }

    public Iterable<IndigoAtom> atoms() {
        return new IndigoIterable<>(obj::iterateAtoms, o -> new IndigoAtom(session, o));
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
