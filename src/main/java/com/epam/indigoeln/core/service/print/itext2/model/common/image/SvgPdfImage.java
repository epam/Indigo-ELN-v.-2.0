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
package com.epam.indigoeln.core.service.print.itext2.model.common.image;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;

/**
 * Implementation of PdfImage.
 */
public class SvgPdfImage implements PdfImage {
    private static float pixelsPerPoint = 3;

    private byte[] svgBytes;

    public SvgPdfImage(String svgBase64) {
        if (!StringUtils.isBlank(svgBase64)) {
            svgBytes = Base64.getDecoder().decode(svgBase64);
        }
    }

    @Override
    public Optional<byte[]> getPngBytes(float widthPt) {
        if (svgBytes != null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayInputStream input = new ByteArrayInputStream(svgBytes);
            SvgConverter.convertSvg2Png(input, output, pixelsPerPoint * widthPt);
            return Optional.of(output.toByteArray());
        } else {
            return Optional.empty();
        }
    }
}
