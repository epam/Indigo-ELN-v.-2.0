package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.MiscAPI;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

public interface MiscClient extends MiscAPI {

    @SneakyThrows
    default void loadCompoundsFromFileClient(String filename, java.nio.file.Path tempDirectory, byte[] content) {
        loadCompoundsFromFileClient(ClientUtil.createFileUpload("file", filename, content, tempDirectory));
    }

    @POST
    @Path("/compounds/loadFromFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void loadCompoundsFromFileClient(ClientUtil.ClientUploadForm form);
}
