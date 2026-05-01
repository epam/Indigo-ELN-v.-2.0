package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;

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

    record Attachments () implements TemplateComponent {
    }

    record Batches () implements TemplateComponent {
    }

    record ConceptDetails () implements TemplateComponent {
    }

    record ExperimentDetails () implements TemplateComponent {
    }

    record ExperimentDescription () implements TemplateComponent {
    }

    record PreferredCompoundsDetails () implements TemplateComponent {
    }

    record PreferredCompoundsSummary () implements TemplateComponent {
    }

    record ReactionsDetails () implements TemplateComponent {
    }

    record StoichiometryTable (
        boolean reactantsReagentsSolvents,
        boolean reactionProducts
    ) implements TemplateComponent {
    }

    record ReactionScheme () implements TemplateComponent {
    }

    record Reactants () implements TemplateComponent {
    }

    record IntendedProducts () implements TemplateComponent {
    }
}
