package com.epam.indigoeln.signature.entity;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "Document")
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity extends IdentifiableEntity {

    @NotEmpty
    private String name;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "template_id")
    private SignatureTemplateEntity template;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authorId")
    private UserEntity author;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private DocumentStatus status;

    @NotNull
    private ZonedDateTime createdDate;

    @NotNull
    private ZonedDateTime lastModifiedDate;

    @NotNull
    @OrderBy("ordinal")
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentSignatureEntity> signatures = new ArrayList<>(0);

    @NotEmpty
    private String filename;

    @Lob
    @NotNull
    @JdbcTypeCode(Types.BINARY)
    private byte[] content;
}
