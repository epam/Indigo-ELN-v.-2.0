package com.epam.indigoeln.eln.api;

import com.epam.indigoeln.eln.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TemplateAPI extends BaseAPI {

    @POST
    @Path("/templates")
    TemplateDetailsDTO createTemplate(TemplateRequest request);

    @GET
    @Path("/templates")
    Page<TemplateDTO> getTemplates(@BeanParam Paging paging);

    @GET
    @Path("/templates/by-name/{name}")
    TemplateDetailsDTO getByName(@PathParam("name") String name);

    @GET
    @Path("/templates/{templateId}")
    TemplateDetailsDTO getTemplate(@PathParam("templateId") UUID templateId);

    @PATCH
    @Path("/templates/{templateId}")
    TemplateDetailsDTO editTemplate(@PathParam("templateId") UUID templateId, TemplateEditRequest request);
}
