package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoMolecule extends AbstractIndigoObject {

    IndigoMolecule(IndigoAPI indigo, IndigoObject obj) {
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

    public String molecularFormula() {
        return obj.molecularFormula();
    }

    public double molecularWeight() {
        return obj.molecularWeight();
    }

    public double monoisotopicMass() {
        return obj.monoisotopicMass();
    }

    public void remove() {
        obj.remove();
    }

    public void setProperty(String property, String value) {
        obj.setProperty(property, value);
    }

    @Override
    public String toString() {
        return obj.grossFormula();
    }
}
