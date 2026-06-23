package com.epam.indigoeln.eln.api;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path(BaseAPI.BASE_PATH)
public interface IncidentAPI extends BaseAPI {

    @POST
    @Path("/incidents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void createIncidentReport(@Valid IncidentReportForm form);
}
