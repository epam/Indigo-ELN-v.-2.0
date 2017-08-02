package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.epam.indigoeln.core.service.print.itext2.model.common.DescriptionModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;

public class DescriptionSection extends BasePdfSectionWithSimpleTitle<DescriptionModel> {
    public DescriptionSection(DescriptionModel model) {
        super(model, model.getEntity() + " DESCRIPTION");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(1, width);
        table.addCell(CellFactory.getCommonCellWithHtml(model.getDescription()));
        return table;
    }
}
