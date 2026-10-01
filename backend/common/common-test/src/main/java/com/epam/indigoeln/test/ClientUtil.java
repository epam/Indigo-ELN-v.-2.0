package com.epam.indigoeln.test;

import com.epam.indigoeln.common.model.UploadForm;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;

public class ClientUtil {

    @SneakyThrows
    public static UploadForm uploadForm(String filename, byte[] content) {
        File tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("upload-").toFile();
            FileUtils.forceDeleteOnExit(tmpDir);
            File tmpFile = new File(tmpDir, filename);
            Files.write(tmpFile.toPath(), content);
            return new UploadForm(tmpFile);
        } catch (Exception e) {
            FileUtils.deleteQuietly(tmpDir);
            throw e;
        }
    }
}
