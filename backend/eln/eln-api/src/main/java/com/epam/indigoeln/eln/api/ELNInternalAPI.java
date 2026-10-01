package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.common.model.DocumentStatus;
import com.epam.indigoeln.common.model.UploadForm;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
