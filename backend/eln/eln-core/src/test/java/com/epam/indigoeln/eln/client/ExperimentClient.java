package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.ExperimentAPI;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.model.MutationResponse;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.test.ClientUtil;
import com.epam.indigoeln.test.ClientUtil.ClientUploadForm;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.util.List;
import java.util.UUID;

public interface ExperimentClient extends ExperimentAPI {

    @POST
    @Path("/experiments/{experimentId}/mutate")
    JsonNode mutateExperimentModel4Raw(@PathParam("experimentId") UUID experimentId, @QueryParam("revision") Integer revision, String mutation);

    @POST
    @Path("/experiments/{experimentId}/datamodel/reactions/{reactionAnchor}/importSDF")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    MutationResponse importSDF(@PathParam("experimentId") UUID experimentId, @PathParam("reactionAnchor") ReactionAnchor reactionAnchor, ClientUploadForm form);
}
