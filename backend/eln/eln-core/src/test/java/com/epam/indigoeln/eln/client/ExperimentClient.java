package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.ExperimentAPI;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import java.util.UUID;

public interface ExperimentClient extends ExperimentAPI {

    @POST
    @Path("/experiments/{experimentId}/mutate")
    JsonNode mutateExperimentModel4Raw(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, String mutation);
}
