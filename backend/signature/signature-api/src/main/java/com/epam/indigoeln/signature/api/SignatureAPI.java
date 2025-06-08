package com.epam.indigoeln.signature.api;

import com.epam.indigoeln.signature.model.Document;
import com.epam.indigoeln.signature.model.Template;
import com.epam.indigoeln.signature.model.TemplateRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.PartType;

import java.util.List;

@Path(SignatureAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SignatureAPI {

    String BASE_PATH = "/api/signature";

    @POST
    @Path("/templates")
    Template createTemplate(TemplateRequest template);

    @GET
    @Path("/templates")
    List<Template> getTemplates();

    @POST
    @Path("/documents/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Document uploadDocument(FileUploadForm form);

    @GET
    @Path("/documents")
    List<Document> getDocuments();

    @POST
    @Path("/documents/{documentId}/sign")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Document signDocument(@PathParam("documentId") int documentId, SignForm form);

    @POST
    @Path("/documents/{documentId}/reject")
    Document rejectDocument(@PathParam("documentId") int documentId);

    @GET
    @Path("/documents/{documentId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response downloadDocument(@PathParam("documentId") int documentId);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class FileUploadForm {

        @NotNull
        @FormParam("templateId")
        private Integer templateId;

        @NotEmpty
        @FormParam("name")
        private String name;

        @NotEmpty
        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        private byte[] file;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class SignForm {

        @NotNull
        @FormParam("keyStore")
        private byte[] keyStore;

        @NotNull
        @FormParam("keyStorePassword")
        private String keyStorePassword;
    }
}
