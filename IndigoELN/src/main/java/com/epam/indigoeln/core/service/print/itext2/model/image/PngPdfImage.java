package com.epam.indigoeln.core.service.print.itext2.model.image;

public class PngPdfImage implements PdfImage {
    private byte[] pngBytes;

    public PngPdfImage(byte[] bytes) {
        this.pngBytes = bytes;
    }

    @Override
    public byte[] getPngBytes(float widthPt) {
        return pngBytes;
    }
}
