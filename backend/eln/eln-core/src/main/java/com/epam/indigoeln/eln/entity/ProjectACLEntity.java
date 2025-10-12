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
@Entity(name = "ProjectACL")
@IdClass(ProjectACLEntity.CompositeID.class)
public class ProjectACLEntity implements BaseACLEntity {

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    private ProjectEntity project;

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    private UserEntity user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AccessLevel level;

    @Override
    @Transient
    public Boolean getInherited() {
        return Boolean.FALSE;
    }

    @Override
    public void setInherited(Boolean inherited) {
        // nothing
    }

    public record CompositeID(
            ProjectEntity project,
            UserEntity user
    ) implements Serializable {}
}
