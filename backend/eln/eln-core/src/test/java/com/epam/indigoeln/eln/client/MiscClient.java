package com.epam.indigoeln.eln.client;

import com.epam.indigoeln.eln.api.MiscAPI;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import lombok.SneakyThrows;

import java.nio.file.Path;

public interface MiscClient extends MiscAPI {

    @SneakyThrows
    default void loadCompoundsFromFileClient(String filename, Path tempDirectory, byte[] content) {
        loadCompoundsFromFileClient(ClientUtil.createFileUpload("file", filename, content, tempDirectory));
    }

    @POST
    @jakarta.ws.rs.Path("/compounds/loadFromFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void loadCompoundsFromFileClient(ClientUtil.ClientUploadForm form);
}
