package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SignatureAPI extends BaseAPI {

    @POST
    @Path("/signature/templates")
    SignatureTemplateDetailsDTO createSignatureTemplate(SignatureTemplateRequest request);

    @GET
    @Path("/signature/templates")
    Page<SignatureTemplateDTO> getSignatureTemplates(@BeanParam Paging paging);

    @GET
    @Path("/signature/templates/{signatureTemplateId}")
    SignatureTemplateDetailsDTO getSignatureTemplate(@PathParam("signatureTemplateId") UUID signatureTemplateId);

    @PATCH
    @Path("/signature/templates/{signatureTemplateId}")
    SignatureTemplateDetailsDTO editSignatureTemplate(@PathParam("signatureTemplateId") UUID signatureTemplateId, SignatureTemplateEditRequest request);

    @GET
    @Path("/signature/experiments/pending")
    Page<ExperimentForSignatureDTO> getExperimentsForSignature(Paging paging);
}
