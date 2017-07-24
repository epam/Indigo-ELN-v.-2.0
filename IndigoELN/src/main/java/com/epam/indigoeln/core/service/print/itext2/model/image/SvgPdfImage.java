package com.epam.indigoeln.core.service.print.itext2.model.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class SvgPdfImage implements PdfImage {
    private static  float PIXELS_PER_POINT = 3;

    private  byte[] svgBytes;

    public SvgPdfImage(String svgBase64) {
        svgBytes = Base64.getDecoder().decode(svgBase64);
    }

    @Override
    public byte[] getPngBytes(float widthPt) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayInputStream input = new ByteArrayInputStream(svgBytes);
        SvgConverter.convertSvg2Png(input, output, PIXELS_PER_POINT * widthPt);

        return output.toByteArray();
    }
}
