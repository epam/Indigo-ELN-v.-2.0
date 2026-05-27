package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoSDFSaver extends AbstractIndigoObject {

    IndigoSDFSaver(IndigoAPI indigo, IndigoObject obj) {
        super(indigo, obj);
    }

    public void sdfAppend(IndigoMolecule item) {
        obj.sdfAppend(item.obj);
    }

    public void close() {
        obj.close();
    }
}
