package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.model.AccessLevel;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.UUID;

public class ACLEntryArrayType extends AbstractArrayOfStructType<List<ACLEntry>, ACLEntry> {

    @Override
    public Class<List<ACLEntry>> returnedClass() {
        //noinspection unchecked,rawtypes
        return (Class) List.class;
    }

    @Override
    protected ACLEntry doRead(String[] parts) {
        return new ACLEntry(UUID.fromString(parts[0]), parts[1], parts[2], AccessLevel.valueOf(parts[3]), "t".equals(parts[4]));
    }

    @Override
    protected List<ACLEntry> doAssemble(StreamEx<ACLEntry> stream) {
        return stream.toImmutableList();
    }

    @Override
    protected StreamEx<ACLEntry> doDisassemble(List<ACLEntry> value) {
        return StreamEx.of(value);
    }

    @Override
    protected Object[] doWrite(ACLEntry item) {
        return new Object[]{item.getUserId(), item.getDisplayName(), item.getUsername(), item.getLevel(), item.isInherited()};
    }

    @Override
    protected String getSQLElementType() {
        return "ACL_Entry";
    }
}
