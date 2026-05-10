package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.config.APISecretHeaderFactory;
import com.epam.indigoeln.common.model.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.io.File;
import java.util.UUID;

@Path(ELNInternalAPI.BASE_PATH)
@RegisterRestClient(configKey = "eln-internal-api")
@RegisterClientHeaders(APISecretHeaderFactory.class)
public interface ELNInternalClient extends ELNInternalAPI {

    @POST
    @Path("/internal/signatureUpdated")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void internalSignatureUpdatedClient(@NotNull @QueryParam("documentId") UUID documentId, @NotNull @QueryParam("message") String message, @QueryParam("documentStatus") DocumentStatus updatedStatus, @FormParam("file") File form);
}
