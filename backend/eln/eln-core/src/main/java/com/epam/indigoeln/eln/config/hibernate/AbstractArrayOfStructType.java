package com.epam.indigoeln.eln.config.hibernate;

import com.epam.indigoeln.eln.util.ModelUtil;
import one.util.streamex.StreamEx;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.*;
import java.util.Objects;

public abstract class AbstractArrayOfStructType<T, E> implements UserType<T> {

    protected abstract E doRead(String[] parts);

    protected abstract T doAssemble(StreamEx<E> stream);

    protected StreamEx<E> doDisassemble(T value) {
        throw new UnsupportedOperationException();
    }

    protected Object[] doWrite(E item) {
        throw new UnsupportedOperationException();
    }

    protected abstract String getSQLElementType();

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public T deepCopy(T value) {
        return value;
    }

    @Override
    @Nullable
    public T nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        Object[] array = (Object[]) rs.getArray(position).getArray();
        if (array == null) {
            return null;
        }
        StreamEx<E> stream = StreamEx.of(array)
                .map(PGobject.class::cast)
                .map(o -> ModelUtil.splitPostgresStruct(Objects.requireNonNull(o.getValue())))
                .map(this::doRead);
        return doAssemble(stream);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, @Nullable T value, int position, WrapperOptions options) throws SQLException {
        if (value == null) {
            st.setNull(position, Types.ARRAY);
            return;
        }
        String[] strings = doDisassemble(value)
                .map(this::doWrite)
                .map(ModelUtil::combinePostgresStruct)
                .toArray(String[]::new);
        Array array = st.getConnection().createArrayOf(getSQLElementType(), strings);
        st.setArray(position, array);
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
