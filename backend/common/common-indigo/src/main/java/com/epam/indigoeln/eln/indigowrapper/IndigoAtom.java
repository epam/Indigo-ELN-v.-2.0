package com.epam.indigoeln.eln.indigowrapper;

import com.epam.indigo.IndigoObject;

public class IndigoAtom extends AbstractIndigoObject {

    IndigoAtom(IndigoAPI indigo, IndigoObject obj) {
        super(indigo, obj);
    }

    public Integer charge() {
        return obj.charge();
    }
}
