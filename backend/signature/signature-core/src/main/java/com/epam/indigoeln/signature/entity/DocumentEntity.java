package com.epam.indigoeln.signature.entity;

import com.epam.indigoeln.signature.model.Status;
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
import java.util.List;

@Getter
@Setter
@Entity(name = "Document")
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotEmpty
    private String name;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "templateId")
    private TemplateEntity template;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authorId")
    private UserEntity author;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Status status;
    @NotNull
    private ZonedDateTime createdDate;
    @NotNull
    private ZonedDateTime lastModifiedDate;
    @NotEmpty
    @OrderColumn(name = "index")
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DocumentSignatureBlockEntity> signatureBlocks;
    @Lob
    @NotNull
    @JdbcTypeCode(Types.BINARY)
    private byte[] content;
}
