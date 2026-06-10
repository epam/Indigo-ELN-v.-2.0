package com.epam.indigoeln.signature.entity;

import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "SignatureTemplate")
@NamedEntityGraph(
        name = "SignatureTemplate.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
        }
)
@NamedEntityGraph(
        name = "SignatureTemplate.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
        }
)
public class SignatureTemplateEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    protected UserEntity createdBy;

    @NotNull
    @Column(updatable = false)
    protected ZonedDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    protected UserEntity modifiedBy;

    @NotNull
    protected ZonedDateTime modifiedAt;

    @NotEmpty
    @Size(max = 256)
    private String name;

    @NotNull
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ordinal")
    private List<SignatureTemplateBlockEntity> blocks = new ArrayList<>(0);
}
