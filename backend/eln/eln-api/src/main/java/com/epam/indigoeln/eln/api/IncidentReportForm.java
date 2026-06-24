package com.epam.indigoeln.eln.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.FormParam;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Data
@NoArgsConstructor
public class IncidentReportForm {

    @NotBlank
    @FormParam("message")
    private String message;

    @Nullable
    @FormParam("experimentId")
    private UUID experimentId;

    @Nullable
    @FormParam("mutation")
    private String mutationJson;

    @Nullable
    @FormParam("file")
    private FileUpload file;
}
