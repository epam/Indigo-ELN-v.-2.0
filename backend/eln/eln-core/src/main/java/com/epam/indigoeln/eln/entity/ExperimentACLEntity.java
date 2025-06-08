package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.AccessLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ExperimentACL")
@Table(name = "Experiment_ACL")
@IdClass(ExperimentACLEntity.CompositeID.class)
public class ExperimentACLEntity implements BaseACLEntity {

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "experiment_id", updatable = false)
    private ExperimentEntity experiment;

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private UserEntity user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AccessLevel level;

    @Basic
    @NotNull
    private Boolean inherited;

    public record CompositeID(
            ExperimentEntity experiment,
            UserEntity user
    ) implements Serializable {}
}
