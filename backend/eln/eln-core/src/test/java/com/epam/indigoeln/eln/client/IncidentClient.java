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

public interface IncidentClient extends IncidentAPI {

    @POST
    @Path("/incidents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void createIncidentReport(ClientIncidentReportForm form);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ClientIncidentReportForm {

        @Nullable
        @FormProperty("url")
        private String url;

        @Nullable
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
