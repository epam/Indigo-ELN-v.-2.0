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
import com.epam.indigo.IndigoRenderer;
import com.epam.indigoeln.config.audit.CustomAuditProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class IndigoProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuditProvider.class);

    private static final String INDIGO_PATH;

    static {
        String libraryPathStr = System.getProperty("indigoeln.library.path");
        if (libraryPathStr != null) {
            try {
                Path libraryPath = Paths.get(libraryPathStr);
                Files.createDirectories(libraryPath);
                Path indigoPath = libraryPath.resolve("libindigo.so");
                try (InputStream is = Indigo.class.getResourceAsStream("/linux-x86_64/libindigo.so")) {
                    Files.copy(is, indigoPath);
                }
                Path indigoRendererPath = libraryPath.resolve("libindigo-renderer.so");
                try (InputStream is = Indigo.class.getResourceAsStream("/linux-x86_64/libindigo-renderer.so")) {
                    Files.copy(is, indigoRendererPath);
                }
                INDIGO_PATH = libraryPathStr;
            } catch (Exception e) {
                LOGGER.error("Failed to extract native libs", e);
                throw new RuntimeException(e);
            }
        } else {
            INDIGO_PATH = null;
        }
    }

    public Indigo indigo() {
        Indigo indigo = new Indigo(INDIGO_PATH);
        indigo.setOption("ignore-stereochemistry-errors", "true");

        return indigo;
    }

    public IndigoRenderer renderer(Indigo indigo) {
        IndigoRenderer renderer = new IndigoRenderer(indigo);

        indigo.setOption("render-label-mode", "hetero");
        indigo.setOption("render-output-format", "svg");
        indigo.setOption("render-coloring", true);
        indigo.setOption("render-margins", 0, 0);

        return renderer;
    }
}
