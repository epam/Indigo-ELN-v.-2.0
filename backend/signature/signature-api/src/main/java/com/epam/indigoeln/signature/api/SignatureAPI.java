package com.epam.indigoeln.signature.api;

import com.epam.indigoeln.common.model.UploadForm;
import com.epam.indigoeln.signature.model.DocumentDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateDetailsDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path(SignatureAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SignatureAPI {

    String BASE_PATH = "/api/signature";

    @POST
    @Path("/templates")
    SignatureTemplateDetailsDTO createTemplate(SignatureTemplateRequest template);

    @GET
    @Path("/templates")
    List<SignatureTemplateDTO> getTemplates();

    @POST
    @Path("/documents/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    DocumentDTO uploadDocument(@QueryParam("name") String name, @QueryParam("templateId") UUID templateId, UploadForm form);

    @GET
    @Path("/documents")
    List<DocumentDTO> getDocuments();

    @GET
    @Path("/documents/{id}")
    DocumentDTO getDocument(@PathParam("id") UUID id);

    @POST
    @Path("/documents/{documentId}/sign")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    DocumentDTO signDocument(@PathParam("documentId") UUID documentId);

    @POST
    @Path("/documents/{documentId}/reject")
    DocumentDTO rejectDocument(@PathParam("documentId") UUID documentId);

    @GET
    @Path("/documents/{documentId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response downloadDocument(@PathParam("documentId") UUID documentId);
}
