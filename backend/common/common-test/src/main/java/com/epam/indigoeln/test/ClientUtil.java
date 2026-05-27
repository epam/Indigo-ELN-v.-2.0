package com.epam.indigoeln.test;

import feign.form.FormData;
import feign.form.FormProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

public class ClientUtil {

    @SneakyThrows
    public static ClientUploadForm createFileUpload(String filename, byte[] content) {
        FormData file = new FormData(MediaType.APPLICATION_OCTET_STREAM, filename, content);
        return new ClientUploadForm(file);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientUploadForm {

        @NotNull
        @FormProperty("file")
        private FormData file;
    }
}
