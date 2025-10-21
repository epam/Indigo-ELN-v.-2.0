package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class SignatureTemplateEntity extends BaseEntity {

    @NotEmpty
    private String name;

    @NotNull
    @ElementCollection
    @CollectionTable(name = "signature_template_block", joinColumns = @JoinColumn(name = "signature_template_id"))
    @OrderColumn(name = "ordinal")
    private List<SignatureTemplateBlockEmbedded> blocks = new ArrayList<>(0);
}
