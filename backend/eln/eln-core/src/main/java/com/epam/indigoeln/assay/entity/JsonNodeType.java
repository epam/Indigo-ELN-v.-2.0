package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.eln.config.hibernate.AbstractJsonUserType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Maps a free-form JSONB column to a Jackson {@link JsonNode}. Used for the flexible
 * assay definition / calculation parameter / value payload columns where the shape is
 * driven by the assay configuration rather than a fixed schema.
 */
public class JsonNodeType extends AbstractJsonUserType<JsonNode> {

    public JsonNodeType() {
        super(JsonNode.class);
    }
}
