package com.epam.indigoeln.signature.entity;

import com.epam.indigoeln.signature.model.Reason;
import com.epam.indigoeln.signature.model.SignatureStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity(name = "DocumentSignatureBlock")
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSignatureBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "documentId")
    private DocumentEntity document;
    @Column(name = "index")
    private int index;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "templateBlockId")
    private TemplateSignatureBlockEntity templateBlock;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "userId")
    private UserEntity user;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Reason reason;
    @Temporal(TemporalType.TIMESTAMP)
    private ZonedDateTime actionDate;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SignatureStatus status;
    private String comment;
}
