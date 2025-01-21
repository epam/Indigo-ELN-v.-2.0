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
package com.epam.indigoeln.config.bingo;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoRenderer;
import com.epam.indigoeln.config.audit.CustomAuditProvider;
import com.google.common.base.Strings;
import com.sun.jna.Platform;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.nio.file.*;

@Slf4j
@Configuration
public class IndigoProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuditProvider.class);

    private String indigoPath;

    private synchronized void extractLibs(String libraryPathStr) {
        if (indigoPath != null) {
            return;
        }
        try {
            Path libraryPath = !Strings.isNullOrEmpty(libraryPathStr) ? Paths.get(libraryPathStr) : Files.createTempDirectory("indigo-lib");
            String platformName;
            String libraryPattern;
            if (Platform.isLinux()) {
                platformName = "linux";
                libraryPattern = "*.so";
            } else if (Platform.isWindows()) {
                platformName = "windows";
                libraryPattern = "*.dll";
            } else if (Platform.isMac()) {
                platformName = "darwin";
                libraryPattern = "*.dylib";
            } else {
                throw new RuntimeException("Unsupported platform");
            }
            if (Platform.isARM() && Platform.is64Bit()) {
                platformName += "-aarch64";
            } else if (Platform.is64Bit()) {
                platformName += "-x86_64";
            } else {
                platformName += "-i386";
            }
            Path platformLibraryPath = libraryPath.resolve(platformName);
            log.info("Using indigo native library path: {}", platformLibraryPath.toAbsolutePath());
            Files.createDirectories(platformLibraryPath);
            System.setProperty("jna.library.path", platformLibraryPath.toAbsolutePath().toString());
            // find all *.dll files in classpath package
            for (Resource resource : new PathMatchingResourcePatternResolver().getResources("classpath*:/" + platformName + "/" + libraryPattern)) {
                Path path = platformLibraryPath.resolve(resource.getFilename());
                try (InputStream is = resource.getInputStream()) {
                    Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    LOGGER.error("Failed to extract native libs", e);
                    throw new RuntimeException(e);
                }
            }
            this.indigoPath = libraryPath.toAbsolutePath().toString();
        } catch (Exception e) {
            LOGGER.error("Failed to extract native libs", e);
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Indigo indigo(@Value("${indigoeln.library.path:}") String libraryPath) {
        log.info("!!! Indigo being created");
        extractLibs(libraryPath);
        Indigo indigo = new Indigo(indigoPath);
        indigo.setOption("ignore-stereochemistry-errors", "true");

        return indigo;
    }

    @Bean
    public IndigoRenderer renderer(Indigo indigo) {
        log.info("!!! IndigoRenderer being created");
        IndigoRenderer renderer = new IndigoRenderer(indigo);

        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-output-format", "svg");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 0, 0);

        return renderer;
    }

    @Bean
    public IndigoInchi inchi(Indigo indigo) {
        log.info("!!! IndigoInchi being created");
        IndigoInchi inchi = new IndigoInchi(indigo);
        return inchi;
    }
}
