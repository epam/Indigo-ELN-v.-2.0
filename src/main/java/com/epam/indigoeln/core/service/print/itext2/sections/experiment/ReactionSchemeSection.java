package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.ReactionSchemeModel;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public class ReactionSchemeSection extends BasePdfSectionWithSimpleTitle<ReactionSchemeModel> {
    public ReactionSchemeSection(ReactionSchemeModel model) {
        super(model, "REACTION SCHEME");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(1, width);
        PdfPCell cell = CellFactory.getImageCell(model.getImage(), width);
        table.addCell(cell);
        return table;
    }

}
