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
package com.epam.indigoeln.core.service.print.itext2.utils;

import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PngPdfImage;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Class provides functionality for loading logo.
 */
public final class LogoUtils {
    private static final String LOGO_FILE_NAME = "pdf/logo_new_blue.png";

    private LogoUtils() {
    }

    /**
     * Loads logo from resources.
     *
     * @return Logo's pdf image
     */
    public static PdfImage loadDefaultLogo() {
        try {
            ClassLoader cl = LogoUtils.class.getClassLoader();
            InputStream resourceAsStream = cl.getResourceAsStream(LOGO_FILE_NAME);
            return new PngPdfImage(IOUtils.toByteArray(resourceAsStream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

