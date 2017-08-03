package com.epam.indigoeln.core.service.print.itext2.model.common.image;

import java.util.Optional;

public interface PdfImage {
    Optional<byte[]> getPngBytes(float widthPt);
}
