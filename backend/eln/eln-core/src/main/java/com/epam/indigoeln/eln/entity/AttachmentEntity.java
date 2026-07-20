package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

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

    // At most one of these is set - an attachment belongs to exactly one project, notebook, or experiment,
    // chosen at creation time. Modeled as three nullable @ManyToOne rather than a polymorphic parent
    // reference so each parent side can use a plain @OneToMany(mappedBy) collection.
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id")
    private NotebookEntity notebook;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private ExperimentEntity experiment;

    @NotEmpty
    private String name;

    @NotNull
    private Long size;

    @NotNull
    private Boolean deleted;

    @Basic(fetch = FetchType.LAZY)
    private byte @NotNull[] content;
}
