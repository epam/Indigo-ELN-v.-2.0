package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.common.model.UploadForm;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ELNInternalAPI extends BaseAPI {

    String BASE_PATH = "/internalapi/eln";

    @POST
    @Path("/signatureUpdated")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void internalSignatureUpdated(@NotNull @QueryParam("documentId") UUID documentId, @NotNull @QueryParam("message") String message, @QueryParam("documentStatus") DocumentStatus updatedStatus, UploadForm form);
}
