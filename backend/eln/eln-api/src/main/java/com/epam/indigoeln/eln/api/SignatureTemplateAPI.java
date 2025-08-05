package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SignatureTemplateAPI extends BaseAPI {

    @POST
    @Path("/signatureTemplates")
    SignatureTemplateDetailsDTO createSignatureTemplate(SignatureTemplateRequest request);

    @GET
    @Path("/signatureTemplates")
    Page<SignatureTemplateDTO> getSignatureTemplates(@BeanParam Paging paging);

    @GET
    @Path("/signatureTemplates/{signatureTemplateId}")
    SignatureTemplateDetailsDTO getSignatureTemplate(@PathParam("signatureTemplateId") UUID signatureTemplateId);

    @PATCH
    @Path("/signatureTemplates/{signatureTemplateId}")
    SignatureTemplateDetailsDTO editSignatureTemplate(@PathParam("signatureTemplateId") UUID signatureTemplateId, SignatureTemplateEditRequest request);
}
