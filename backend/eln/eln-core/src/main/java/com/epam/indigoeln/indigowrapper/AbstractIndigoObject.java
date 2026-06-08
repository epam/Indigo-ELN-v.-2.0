package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractIndigoObject {

    protected final IndigoAPI session;
    protected final IndigoObject obj;

    public Map<String, String> getProperties() {
        Map<String, String> map = new LinkedHashMap<>();
        for (IndigoObject it = obj.iterateProperties(); it.hasNext(); ) {
            IndigoObject prop = it.next();
            String name = prop.name();
            map.put(name, obj.getProperty(name));
        }
        return map;
    }
}
