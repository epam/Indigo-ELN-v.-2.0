package com.epam.indigoeln.eln.indigowrapper;

import com.epam.indigo.IndigoObject;

import java.util.Iterator;

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

    /**
     * This molecule, or a laid-out copy when its atoms carry no 2D coordinates. A SMILES or InChI
     * has none, and a molfile written without them puts every atom on one point — which a sketcher
     * draws as a dot. A copy, because this may be a component of a reaction.
     */
    public IndigoMolecule laidOut() {
        if (hasLayout()) {
            return this;
        }
        IndigoObject copy = obj.clone();
        copy.layout();
        return new IndigoMolecule(session, copy);
    }

    private boolean hasLayout() {
        if (!obj.hasCoord()) {
            return false;
        }
        Iterator<IndigoAtom> atoms = atoms().iterator();
        if (!atoms.hasNext()) {
            return true;
        }
        float[] first = atoms.next().xyz();
        if (!atoms.hasNext()) {
            // a single atom has nothing to lay out
            return true;
        }
        while (atoms.hasNext()) {
            float[] xyz = atoms.next().xyz();
            // z aside: 3D coordinates differing only in depth still draw as a dot
            if (xyz[0] != first[0] || xyz[1] != first[1]) {
                return true;
            }
        }
        return false;
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
