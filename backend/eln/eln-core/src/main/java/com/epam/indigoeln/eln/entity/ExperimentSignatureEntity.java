package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.SignatureReason;
import com.epam.indigoeln.eln.model.SignatureStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ExperimentSignature")
@Table(name = "Experiment_Signature")
public class ExperimentSignatureEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "experiment_id")
    private ExperimentEntity experiment;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SignatureReason reason;

    @Nullable
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SignatureStatus status;

    @Nullable
    @Column(name = "signed_at")
    protected ZonedDateTime signedAt;
}
