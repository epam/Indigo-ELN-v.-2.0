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
