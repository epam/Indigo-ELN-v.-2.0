package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.Getter;

@RegisterForReflection
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TemplateComponent.ReactionScheme.class, name = "reactionScheme"),
        @JsonSubTypes.Type(value = TemplateComponent.StoichiometryTable.class, name = "stoichiometryTable"),
        @JsonSubTypes.Type(value = TemplateComponent.Batches.class, name = "batches"),
        @JsonSubTypes.Type(value = TemplateComponent.Attachments.class, name = "attachments"),
        @JsonSubTypes.Type(value = TemplateComponent.ExperimentDescription.class, name = "experimentDescription"),
        @JsonSubTypes.Type(value = TemplateComponent.ExperimentDetails.class, name = "experimentDetails"),
        @JsonSubTypes.Type(value = TemplateComponent.ConceptDetails.class, name = "conceptDetails"),
})
public sealed interface TemplateComponent permits
        TemplateComponent.ReactionScheme,
        TemplateComponent.StoichiometryTable,
        TemplateComponent.Batches,
        TemplateComponent.Attachments,
        TemplateComponent.ExperimentDescription,
        TemplateComponent.ExperimentDetails,
        TemplateComponent.ConceptDetails
{

    final class ReactionScheme implements TemplateComponent {
    }

    @Data
    final class StoichiometryTable implements TemplateComponent {
        private boolean reactantsReagentsSolvents;
        private boolean reactionProduct;
    }

    // preferred compound details - for later

    final class Batches implements TemplateComponent {
    }

    final class Attachments implements TemplateComponent {
    }

    final class ExperimentDescription implements TemplateComponent {
    }

    final class ExperimentDetails implements TemplateComponent {
    }

    final class ConceptDetails implements TemplateComponent {
    }
}
