package com.epam.indigoeln.eln.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.FormParam;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
public class IncidentReportForm {

    @Nullable
    @FormParam("url")
    private String url;

    @NotBlank
    @FormParam("message")
    private String message;

    @Nullable
    @FormParam("experiment")
    private String experiment;

    @Nullable
    @FormParam("requestURL")
    private String requestURL;

    @Nullable
    @FormParam("requestMethod")
    private String requestMethod;

    @Nullable
    @FormParam("requestBody")
    private String requestBody;

    @Nullable
    @FormParam("responseBody")
    private String responseBody;

    @Nullable
    @FormParam("file")
    private FileUpload file;
}
