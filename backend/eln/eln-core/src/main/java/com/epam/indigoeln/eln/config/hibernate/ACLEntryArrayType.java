package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.model.AccessLevel;
import one.util.streamex.StreamEx;

import java.util.UUID;

public class ACLEntryArrayType extends AbstractArrayOfStructType<ACLEntry[], ACLEntry> {

    @Override
    public Class<ACLEntry[]> returnedClass() {
        return ACLEntry[].class;
    }

    @Override
    protected ACLEntry doRead(String[] parts) {
        return new ACLEntry(UUID.fromString(parts[0]), parts[1], parts[2], AccessLevel.valueOf(parts[3]), "t".equals(parts[4]));
    }

    @Override
    protected ACLEntry[] doAssemble(StreamEx<ACLEntry> stream) {
        return stream.toArray(ACLEntry[]::new);
    }

    @Override
    protected StreamEx<ACLEntry> doDisassemble(ACLEntry[] value) {
        return StreamEx.of(value);
    }

    @Override
    protected Object[] doWrite(ACLEntry item) {
        return new Object[]{item.getUserId(), item.getDisplayName(), item.getUsername(), item.getLevel(), item.getInherited()};
    }

    @Override
    protected String getSQLElementType() {
        return "ACL_Entry";
    }
}
