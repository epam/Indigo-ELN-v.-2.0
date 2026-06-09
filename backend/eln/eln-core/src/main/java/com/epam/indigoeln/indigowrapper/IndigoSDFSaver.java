package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoSDFSaver extends AbstractIndigoObject implements AutoCloseable {

    IndigoSDFSaver(IndigoAPI indigo, IndigoObject obj) {
        super(indigo, obj);
    }

    public void sdfAppend(IndigoMolecule item) {
        obj.sdfAppend(item.obj);
    }

    @Override
    public void close() {
        obj.close();
    }
}
