package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Value;

@RegisterForReflection
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TemplateComponent.Attachments.class, name = "attachments"),
        @JsonSubTypes.Type(value = TemplateComponent.Batches.class, name = "batches"),
        @JsonSubTypes.Type(value = TemplateComponent.ConceptDetails.class, name = "conceptDetails"),
        @JsonSubTypes.Type(value = TemplateComponent.ExperimentDetails.class, name = "experimentDetails"),
        @JsonSubTypes.Type(value = TemplateComponent.ExperimentDescription.class, name = "experimentDescription"),
        @JsonSubTypes.Type(value = TemplateComponent.PreferredCompoundsDetails.class, name = "preferredCompoundsDetails"),
        @JsonSubTypes.Type(value = TemplateComponent.PreferredCompoundsSummary.class, name = "preferredCompoundsSummary"),
        @JsonSubTypes.Type(value = TemplateComponent.ReactionsDetails.class, name = "reactionsDetails"),
        @JsonSubTypes.Type(value = TemplateComponent.StoichiometryTable.class, name = "stoichiometryTable"),
        @JsonSubTypes.Type(value = TemplateComponent.ReactionScheme.class, name = "reactionScheme"),
        @JsonSubTypes.Type(value = TemplateComponent.Reactants.class, name = "reactants"),
        @JsonSubTypes.Type(value = TemplateComponent.IntendedProducts.class, name = "intendedProducts")
})

public sealed interface TemplateComponent permits
        TemplateComponent.Attachments,
        TemplateComponent.Batches,
        TemplateComponent.ConceptDetails,
        TemplateComponent.ExperimentDetails,
        TemplateComponent.ExperimentDescription,
        TemplateComponent.PreferredCompoundsDetails,
        TemplateComponent.PreferredCompoundsSummary,
        TemplateComponent.ReactionsDetails,
        TemplateComponent.StoichiometryTable,
        TemplateComponent.ReactionScheme,
        TemplateComponent.Reactants,
        TemplateComponent.IntendedProducts {

    final class Attachments implements TemplateComponent {
    }

    final class Batches implements TemplateComponent {
    }

    final class ConceptDetails implements TemplateComponent {
    }

    final class ExperimentDetails implements TemplateComponent {
    }

    final class ExperimentDescription implements TemplateComponent {
    }

    final class PreferredCompoundsDetails implements TemplateComponent {
    }

    final class PreferredCompoundsSummary implements TemplateComponent {
    }

    final class ReactionsDetails implements TemplateComponent {
    }

    @Value
    @AllArgsConstructor(onConstructor_ = @JsonCreator)
    class StoichiometryTable implements TemplateComponent {
        boolean reactantsReagentsSolvents;
        boolean reactionProducts;
    }

    final class ReactionScheme implements TemplateComponent {
    }

    final class Reactants implements TemplateComponent {
    }

    final class IntendedProducts implements TemplateComponent {
    }
}
