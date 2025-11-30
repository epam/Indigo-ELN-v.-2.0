package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.AccessLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "NotebookACL")
@IdClass(NotebookACLEntity.CompositeID.class)
public class NotebookACLEntity implements BaseACLEntity {

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    private NotebookEntity notebook;

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    private UserEntity user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AccessLevel level;

    public record CompositeID(
            NotebookEntity notebook,
            UserEntity user
    ) implements Serializable {}
}
