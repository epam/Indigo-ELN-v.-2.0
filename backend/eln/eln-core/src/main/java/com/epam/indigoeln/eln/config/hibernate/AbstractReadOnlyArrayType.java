package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.util.ModelUtil;
import one.util.streamex.StreamEx;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public abstract class AbstractReadOnlyArrayType<T> implements UserType<T> {

    protected abstract T doRead(StreamEx<String[]> stream);

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public boolean equals(T x, T y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(T x) {
        return Objects.hashCode(x);
    }

    @Override
    public T deepCopy(T value) {
        return value;
    }

    @Override
    public T nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Object[] array = (Object[]) rs.getArray(position).getArray();
        // TODO how to map to array of struct?
        StreamEx<String[]> stream = StreamEx.of(array)
                .map(PGobject.class::cast)
                .map(o -> ModelUtil.splitPostgresStruct(Objects.requireNonNull(o.getValue())));
        return doRead(stream);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, T value, int index, SharedSessionContractImplementor session) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(T value) {
        return (Serializable) value;
    }

    @Override
    public T assemble(Serializable cached, Object owner) {
        //noinspection unchecked
        return (T) cached;
    }
}
