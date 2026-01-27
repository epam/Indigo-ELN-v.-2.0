package com.epam.indigoeln.signature.entity;

import com.epam.indigoeln.signature.model.Reason;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

@Getter
@Setter
@Entity(name = "TemplateSignatureBlock")
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSignatureBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "templateId")
    private TemplateEntity template;
    @Column(name = "index")
    private int index;
    @ManyToOne
    @JoinColumn(name = "userId")
    private UserEntity user;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Reason reason;
}
