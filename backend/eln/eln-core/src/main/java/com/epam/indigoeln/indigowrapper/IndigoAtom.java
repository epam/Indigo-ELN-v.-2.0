package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoAtom extends AbstractIndigoObject {

    IndigoAtom(IndigoSession session, IndigoObject obj) {
        super(session, obj);
    }

    public Integer charge() {
        return obj.charge();
    }
}
