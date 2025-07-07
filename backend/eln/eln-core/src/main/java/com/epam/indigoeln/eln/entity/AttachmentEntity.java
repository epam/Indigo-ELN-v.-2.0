package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Attachment")
@NamedEntityGraph(
        name = "Attachment.download",
        attributeNodes = {
                @NamedAttributeNode("content"),
        }
)
public class AttachmentEntity extends BaseEntity {

    @ManyToMany(mappedBy = "attachments")
    private Set<ProjectEntity> projects = new HashSet<>(0);

    @ManyToMany(mappedBy = "attachments")
    private Set<NotebookEntity> notebooks = new HashSet<>(0);

    @ManyToMany(mappedBy = "attachments")
    private Set<ExperimentEntity> experiments = new HashSet<>(0);

    @NotEmpty
    private String name;

    @NotNull
    private Long size;

    @Basic(fetch = FetchType.LAZY)
    private byte @NotNull[] content;
}
