package com.epam.indigoeln.common.model;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadForm {

    @NotNull
    @FormParam("file")
    private FileUpload file;
}
