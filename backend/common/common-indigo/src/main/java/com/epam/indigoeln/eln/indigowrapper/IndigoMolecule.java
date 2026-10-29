package com.epam.indigoeln.eln.indigowrapper;

import com.epam.indigo.IndigoObject;

import java.util.Arrays;
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
     * Whether the atoms carry 2D coordinates. A SMILES or InChI has none, and a molfile written
     * without them puts every atom on one point — which a sketcher draws as a dot.
     */
    public boolean hasLayout() {
        if (!obj.hasCoord()) {
            return false;
        }
        Iterator<IndigoAtom> atoms = atoms().iterator();
        if (!atoms.hasNext()) {
            return true;
        }
        float[] first = atoms.next().xyz();
        boolean single = true;
        while (atoms.hasNext()) {
            single = false;
            if (!Arrays.equals(atoms.next().xyz(), first)) {
                return true;
            }
        }
        // a single atom has nothing to lay out
        return single;
    }

    public void layout() {
        obj.layout();
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
