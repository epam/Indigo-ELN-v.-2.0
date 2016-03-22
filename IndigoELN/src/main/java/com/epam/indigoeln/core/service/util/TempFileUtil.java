package com.epam.indigoeln.core.service.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class TempFileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileUtil.class);

    public static final String TEMP_FILE_PREFIX = "eln-";

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

        try {
            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(fileData), newFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return newFile;
    }

    public static File getFromTempDirectory(String fileName) {
        String prefixedFileName = getFileNameWithPrefix(fileName);
        File tempDir = FileUtils.getTempDirectory();
        File file = new File(tempDir, prefixedFileName);

        if (!file.exists()) {
            return null;
        }
        return file;
    }
}
