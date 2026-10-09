package com.epam.indigoeln.common.model;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;

@NoArgsConstructor
@SuppressWarnings("NotNullFieldNotInitialized")
public class UploadForm {

    @NotNull
    @FormParam("file")
    private File file;

    @Getter
    @NotNull
    @FormParam("file")
    FileUpload upload;

    public UploadForm(File file) {
        this.file = file;
    }
}
