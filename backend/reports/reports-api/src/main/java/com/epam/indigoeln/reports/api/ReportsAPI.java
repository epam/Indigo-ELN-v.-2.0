package com.epam.indigoeln.reports.api;

import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectDTO;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Value;

@Path(ReportsAPI.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ReportsAPI {

    String BASE_PATH = "/internalapi/reports";

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
