package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.TemplateTab;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Template")
@NamedEntityGraph(
        name = "Template.list",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
        }
)
@NamedEntityGraph(
        name = "Template.details",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
        }
)
public class TemplateEntity extends BaseEntity {

    @NotEmpty
    @Size(max = 256)
    private String name;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    private List<TemplateTab> templateTabs;
}
