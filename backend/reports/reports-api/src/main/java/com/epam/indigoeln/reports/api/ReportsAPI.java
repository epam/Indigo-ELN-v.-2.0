package com.epam.indigoeln.reports.api;

import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectDTO;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Value;

import java.util.Map;

@Path(ReportsAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ReportsAPI {

    String BASE_PATH = "/internalapi/reports";

    @GET
    @Path("/version")
    Map<String, String> getVersion();

    @POST
    @Path("/experiment/")
    @Produces("application/pdf")
    Response generateExperimentReport(ExperimentReportDataDTO data);

    @Value
    class ExperimentReportDataDTO {

        ProjectDTO project;
        ExperimentDetailsDTO experiment;
        String picture; // because it's SVG really, we can use String
        ExperimentModel model;
    }
}
