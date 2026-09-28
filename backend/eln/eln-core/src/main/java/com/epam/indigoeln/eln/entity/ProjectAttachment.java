package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@Entity(name = "ProjectAttachment")
@NamedEntityGraph(
        name = "ProjectAttachment.download",
        attributeNodes = {
                @NamedAttributeNode("content"),
        }
)
public class ProjectAttachment extends AbstractAttachment<ProjectEntity> {

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity parent;
}
