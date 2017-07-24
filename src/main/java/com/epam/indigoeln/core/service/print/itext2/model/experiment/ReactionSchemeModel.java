package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.image.PdfImage;

public class ReactionSchemeModel implements SectionModel {
    private PdfImage image;

    public ReactionSchemeModel(PdfImage image) {
        this.image = image;
    }

    public PdfImage getImage() {
        return image;
    }
}
