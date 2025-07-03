package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@RegisterForReflection
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TemplateComponent.Attachments.class, name = "attachments"),
        @JsonSubTypes.Type(value = TemplateComponent.StoichiometryTable.class, name = "stoichiometryTable"),
})
public sealed abstract class TemplateComponent permits
        TemplateComponent.Attachments,
        TemplateComponent.StoichiometryTable
{

    public static final class Attachments extends TemplateComponent {
    }

    public static final class StoichiometryTable extends TemplateComponent {
        private boolean reactionScheme;
        private boolean reactantsReagentsSolvents;
        private boolean reactionProduct;
    }
}
