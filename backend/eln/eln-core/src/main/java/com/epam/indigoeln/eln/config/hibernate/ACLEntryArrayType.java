package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.model.AccessLevel;
import one.util.streamex.StreamEx;

import java.util.UUID;

public class ACLEntryArrayType extends AbstractReadOnlyArrayType<ACLEntry[]> {

    @Override
    public Class<ACLEntry[]> returnedClass() {
        return ACLEntry[].class;
    }

    @Override
    protected ACLEntry[] doRead(StreamEx<String[]> stream) {
        return stream
                .map(a -> new ACLEntry(UUID.fromString(a[0]), a[1], AccessLevel.valueOf(a[2]), "t".equals(a[3])))
                .toArray(ACLEntry[]::new);
    }
}
