package com.epam.indigoeln.core.service.print.itext2.model.common.image;

import java.util.Optional;

/**
 * Provides methods for working with images in pdf.
 */
public interface PdfImage {
    Optional<byte[]> getPngBytes(float widthPt);
}