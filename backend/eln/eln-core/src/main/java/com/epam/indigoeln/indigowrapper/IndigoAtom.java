package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoAtom extends AbstractIndigoObject {

    IndigoAtom(IndigoSession indigo, IndigoObject obj) {
        super(indigo, obj);
    }

    public Integer charge() {
        return obj.charge();
    }
}
