package com.epam.indigoeln.signature.entity;

import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.signature.model.SignatureReason;
import com.epam.indigoeln.signature.model.SignatureStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Getter
@Setter
@Entity(name = "DocumentSignature")
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSignatureEntity extends IdentifiableEntity {

    @NotNull
    private Integer ordinal;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "template_block_id")
    private SignatureTemplateBlockEntity templateBlock;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SignatureReason reason;

    @Nullable
    private Instant actionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SignatureStatus status;

    @Nullable
    private String comment;
}
