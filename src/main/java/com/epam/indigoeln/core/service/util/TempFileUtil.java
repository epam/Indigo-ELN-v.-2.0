package com.epam.indigoeln.core.service.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TempFileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileUtil.class);

    public static final String TEMP_FILE_PREFIX = "eln-";
    public static final String TEMP_PDF_DONE_SUFFIX = ".pdf.done";

    private TempFileUtil() {
    }

    private static String getFileNameWithPrefix(String fileName) {
        if (StringUtils.startsWith(fileName, TEMP_FILE_PREFIX)) {
            return fileName;
        }

        return String.format("%s%s", TEMP_FILE_PREFIX, fileName);
    }

    public static File saveToTempDirectory(byte[] fileData, String fileName) {
        String prefixedFileName = getFileNameWithPrefix(fileName);

        File tempDir = FileUtils.getTempDirectory();
        File newFile = new File(tempDir, prefixedFileName);

        try (InputStream is = new ByteArrayInputStream(fileData)) {
            FileUtils.copyInputStreamToFile(is, newFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return newFile;
    }
}
