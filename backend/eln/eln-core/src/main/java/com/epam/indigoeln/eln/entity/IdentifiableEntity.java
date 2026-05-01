package com.epam.indigoeln.eln.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class IdentifiableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdentifiableEntity that = (IdentifiableEntity) o;
        //noinspection ConstantValue
        return id != null && that.id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        //noinspection ConstantValue
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
