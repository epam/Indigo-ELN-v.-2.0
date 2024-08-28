/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Util class to work with temporary files.
 */
public final class TempFileUtil {

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

    /**
     * Saves fileData to file in the temp directory.
     *
     * @param fileData data to save
     * @param fileName name of file to create
     * @return created file
     */
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
