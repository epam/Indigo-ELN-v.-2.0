package com.epam.indigoeln.signature.api;

import com.epam.indigoeln.common.config.APISecretHeaderFactory;
import com.epam.indigoeln.signature.model.DocumentDTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.io.File;
import java.util.UUID;

@Path(SignatureAPI.BASE_PATH)
@RegisterRestClient(configKey = "signature-api")
@RegisterClientHeaders(APISecretHeaderFactory.class)
public interface SignatureClient extends SignatureAPI {

    @POST
    @Path("/documents/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    DocumentDTO uploadDocumentClient(@QueryParam("name") String name, @QueryParam("templateId") UUID templateId, @FormParam("file") File form);
}
