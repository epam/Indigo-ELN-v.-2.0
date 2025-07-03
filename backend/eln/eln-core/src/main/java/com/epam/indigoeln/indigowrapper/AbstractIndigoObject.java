package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

public abstract class AbstractIndigoObject {

    protected final IndigoSession session;
    protected final IndigoObject obj;

    AbstractIndigoObject(IndigoSession session, IndigoObject obj) {
        this.session = session;
        this.obj = obj;
    }

    public boolean hasProperty(String prop) {
        return obj.hasProperty(prop);
    }

    public String getProperty(String prop) {
        return obj.getProperty(prop);
    }

    public void setProperty(String prop, String value) {
        obj.setProperty(prop, value);
    }

    public void removeProperty(String prop) {
        obj.removeProperty(prop);
    }

    public IndigoObject iterateProperties() {
        return obj.iterateProperties();
    }

    public void clearProperties() {
        obj.clearProperties();
    }
}
