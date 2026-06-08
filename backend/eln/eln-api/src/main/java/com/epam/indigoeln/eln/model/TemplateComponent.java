package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TemplateComponent.ExperimentDetails.class, name = "experimentDetails"),
        @JsonSubTypes.Type(value = TemplateComponent.ExperimentDescription.class, name = "experimentDescription"),
        @JsonSubTypes.Type(value = TemplateComponent.Attachments.class, name = "attachments"),
        @JsonSubTypes.Type(value = TemplateComponent.StoichiometryTable.class, name = "stoichiometryTable"),
        @JsonSubTypes.Type(value = TemplateComponent.Batches.class, name = "batches"),
        @JsonSubTypes.Type(value = TemplateComponent.VersionHistory.class, name = "versionHistory"),
})

public sealed interface TemplateComponent permits
        TemplateComponent.ExperimentDetails,
        TemplateComponent.ExperimentDescription,
        TemplateComponent.Attachments,
        TemplateComponent.StoichiometryTable,
        TemplateComponent.Batches,
        TemplateComponent.VersionHistory {

    record ExperimentDetails () implements TemplateComponent {
    }

    record ExperimentDescription () implements TemplateComponent {
    }

    record Attachments () implements TemplateComponent {
    }

    record StoichiometryTable (
            boolean reactionScheme,
            boolean reactantsReagentsSolvents,
            boolean intendedProducts
    ) implements TemplateComponent {
    }

    record Batches () implements TemplateComponent {
    }

    record VersionHistory () implements TemplateComponent {
    }
}
