package com.epam.indigoeln.core.service.print.itext2.model.image;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;

public class SvgPdfImage implements PdfImage {
    private static  float PIXELS_PER_POINT = 3;

    private  byte[] svgBytes;

    public SvgPdfImage(String svgBase64) {
        if (!StringUtils.isBlank(svgBase64)){
            svgBytes = Base64.getDecoder().decode(svgBase64);
        }
    }

    @Override
    public Optional<byte[]> getPngBytes(float widthPt) {
        if (svgBytes != null){
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayInputStream input = new ByteArrayInputStream(svgBytes);
            SvgConverter.convertSvg2Png(input, output, PIXELS_PER_POINT * widthPt);
            return Optional.of(output.toByteArray());
        }else {
            return Optional.empty();
        }
    }
}
