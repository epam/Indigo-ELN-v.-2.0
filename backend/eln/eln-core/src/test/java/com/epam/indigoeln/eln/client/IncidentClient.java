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
import lombok.Builder;
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

    default void createIncidentReport(String message,
                                      @Nullable UUID experimentId,
                                      @Nullable String mutationJson,
                                      byte @Nullable [] fileContent,
                                      @Nullable String filename) {
        FormData file = fileContent != null
                ? new FormData(MediaType.APPLICATION_OCTET_STREAM, filename, fileContent)
                : null;
        createIncidentReport(new ClientIncidentReportForm(
                message,
                experimentId != null ? experimentId.toString() : null,
                mutationJson,
                file));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ClientIncidentReportForm {

        @NotBlank
        @FormProperty("url")
        private String url;

        @NotBlank
        @FormProperty("message")
        private String message;

        @Nullable
        @FormProperty("experiment")
        private String experiment;

        @Nullable
        @FormProperty("requestURL")
        private String requestURL;

        @Nullable
        @FormProperty("requestMethod")
        private String requestMethod;

        @Nullable
        @FormProperty("requestBody")
        private String requestBody;

        @Nullable
        @FormProperty("responseBody")
        private String responseBody;

        @Nullable
        @FormProperty("file")
        private FormData file;
    }
}
