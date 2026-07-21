package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@Entity(name = "NotebookAttachment")
@NamedEntityGraph(
        name = "NotebookAttachment.download",
        attributeNodes = {
                @NamedAttributeNode("content"),
        }
)
public class NotebookAttachment extends AbstractAttachment<NotebookEntity> {

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id")
    private NotebookEntity parent;
}
