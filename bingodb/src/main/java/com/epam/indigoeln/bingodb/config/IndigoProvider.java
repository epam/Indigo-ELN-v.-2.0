/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of BingoDB.
 *
 *  BingoDB is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BingoDB is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with BingoDB.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.bingodb.config;

import com.epam.indigo.Indigo;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Provider for Indigo instances creation with common properties.
 */
@Configuration
public class IndigoProvider {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndigoProvider.class);

    /** Path where native libraries will be extracted. */
    @Value("${indigoeln.library.path:}")
    private String indigoLibraryPath;

    /** Extract native libs.
     *
     * @param libraryPathStr path to extract
     */
    private void extractLibs(String libraryPathStr) {
        try {
            Path libraryPath = !Strings.isNullOrEmpty(libraryPathStr)
                    ? Paths.get(libraryPathStr)
                    : Files.createTempDirectory("indigo-lib");
            Path linuxLibraryPath = libraryPath.resolve("linux-x86_64");
            LOGGER.info("Using indigo native library path: {}", linuxLibraryPath.toAbsolutePath());
            Files.createDirectories(linuxLibraryPath);
            Path indigoPath = linuxLibraryPath.resolve("libindigo.so");
            try (InputStream is = Indigo.class.getResourceAsStream("/linux-x86_64/libindigo.so")) {
                Files.copy(is, indigoPath, StandardCopyOption.REPLACE_EXISTING);
            }
            LOGGER.info("Extracted libindigo.so to {}", indigoPath.toAbsolutePath());
            Path bingoPath = linuxLibraryPath.resolve("libbingo-nosql.so");
            try (InputStream is = Indigo.class.getResourceAsStream("/linux-x86_64/libbingo-nosql.so")) {
                Files.copy(is, bingoPath, StandardCopyOption.REPLACE_EXISTING);
            }
            LOGGER.info("Extracted libbingo-nosql.so to {}", bingoPath.toAbsolutePath());
            System.setProperty("jna.library.path", linuxLibraryPath.toAbsolutePath().toString());
        } catch (Exception e) {
            LOGGER.error("Failed to extract native libs", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new Indigo instance with common properties.
     *
     * @return Indigo instance with common properties
     */
    public Indigo indigo() {
        extractLibs(indigoLibraryPath);
        Indigo indigo = new Indigo(indigoLibraryPath);
        indigo.setOption("ignore-stereochemistry-errors", "true");
        return indigo;
    }
}
