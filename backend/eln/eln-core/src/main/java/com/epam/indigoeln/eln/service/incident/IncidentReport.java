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
    @Nullable
    private String message;
    @Nullable
    private JsonNode experimentFrontend;
    @Nullable
    private ExperimentSnapshot experimentBackend;
    @Nullable
    private String requestURL;
    @Nullable
    private String requestMethod;
    @Nullable
    private JsonNode requestBody;
    @Nullable
    private String responseBody;
    @Nullable
    private String attachmentFilename;
}
