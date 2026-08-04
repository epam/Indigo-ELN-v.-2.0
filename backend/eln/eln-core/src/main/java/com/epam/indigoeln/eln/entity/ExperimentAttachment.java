package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@Entity(name = "ExperimentAttachment")
@NamedEntityGraph(
        name = "ExperimentAttachment.download",
        attributeNodes = {
                @NamedAttributeNode("content"),
        }
)
public class ExperimentAttachment extends AbstractAttachment<ExperimentEntity> {

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private ExperimentEntity parent;
}
