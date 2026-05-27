package com.epam.indigoeln.signature.entity;

import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.signature.model.SignatureReason;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "SignatureTemplateBlock")
public class SignatureTemplateBlockEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne
    private SignatureTemplateEntity template;

    @Nullable
    @ManyToOne
    private UserEntity user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SignatureReason reason;
}
