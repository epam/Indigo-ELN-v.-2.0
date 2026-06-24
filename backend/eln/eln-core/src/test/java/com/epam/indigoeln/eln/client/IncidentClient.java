package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.IncidentAPI;
import feign.form.FormData;
import feign.form.FormProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface IncidentClient extends IncidentAPI {

    @POST
    @Path("/incidents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void createIncidentReport(ClientIncidentReportForm form);

    default void createIncidentReport(String description) {
        createIncidentReport(new ClientIncidentReportForm(description, null, null, null));
    }

    default void createIncidentReport(String description,
                                      @Nullable UUID experimentId,
                                      @Nullable String mutationJson,
                                      byte @Nullable [] fileContent,
                                      @Nullable String filename) {
        FormData file = fileContent != null
                ? new FormData(MediaType.APPLICATION_OCTET_STREAM, filename, fileContent)
                : null;
        createIncidentReport(new ClientIncidentReportForm(
                description,
                experimentId != null ? experimentId.toString() : null,
                mutationJson,
                file));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ClientIncidentReportForm {

        @NotBlank
        @FormProperty("description")
        private String description;

        @Nullable
        @FormProperty("experimentId")
        private String experimentId;

        @Nullable
        @FormProperty("mutation")
        private String mutationJson;

        @Nullable
        @FormProperty("file")
        private FormData file;
    }
}
