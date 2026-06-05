package com.epam.indigoeln.eln.config.hibernate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.quarkus.arc.Arc;
import lombok.SneakyThrows;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractJsonUserType<T> implements UserType<T> {

    private final Class<T> clazz;
    @Nullable
    private volatile ObjectMapper objectMapper;

    public AbstractJsonUserType(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public int getSqlType() {
        return Types.OTHER; // PostgreSQL JSON/JSONB
    }

    @Override
    public Class<T> returnedClass() {
        return clazz;
    }

    @Override
    @Nullable
    @SneakyThrows
    public T nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        byte[] json = rs.getBytes(position);
        if (json == null) {
            return null;
        }
        return mapper().readValue(json, clazz);
    }

    @Override
    @SneakyThrows
    public void nullSafeSet(PreparedStatement st, @Nullable T value, int position, WrapperOptions options) throws SQLException {
        if (value == null) {
            st.setNull(position, Types.OTHER);
        } else {
            PGobject pg = new PGobject();
            pg.setType("jsonb");
            pg.setValue(mapper().writeValueAsString(value));
            st.setObject(position, pg);
        }
    }

    @Override
    @Nullable
    @SneakyThrows
    public T deepCopy(@Nullable T value) {
        // Hibernate calls this to snapshot the value at load time.
        // The snapshot is compared via equals() at flush time.
        if (value == null) {
            return null;
        }
        // !!! use proper deepCopy
        T copy = mapper().readValue(mapper().writeValueAsString(value), clazz);
        Preconditions.checkState(copy.equals(value));
        return copy;
    }

    @Override
    public Serializable disassemble(T value) {
        try {
            return mapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T assemble(Serializable cached, Object owner) {
        try {
            return mapper().readValue((String) cached, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectMapper mapper() {
        if (objectMapper == null) {
            objectMapper = Arc.container().instance(ObjectMapper.class).get();
        }
        return checkNotNull(objectMapper);
    }
}
