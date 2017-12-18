package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

/**
 * Implementation of SectionModel interface for reaction scheme.
 */
public class ReactionSchemeModel implements SectionModel {
    private PdfImage image;

    public ReactionSchemeModel(PdfImage image) {
        this.image = image;
    }

    public PdfImage getImage() {
        return image;
    }
}
