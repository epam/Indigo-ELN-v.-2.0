package com.epam.indigoeln.eln.service.incident;

import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentReport {

    private Instant incidentTime;
    private String username;
    private String description;
    @Nullable private ExperimentSnapshot experimentSnapshot;
    @Nullable private JsonNode mutation;
    @Nullable private String attachmentFilename;
}
