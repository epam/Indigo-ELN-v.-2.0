package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public abstract class BasePdfSectionWithSimpleTitle<T extends SectionModel> extends BasePdfSectionWithTableAndTitle<T> {
    private final String title;

    public BasePdfSectionWithSimpleTitle(T model, String title) {
        super(model);
        this.title = title;
    }

    @Override
    protected PdfPTable generateTitleTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(1, width);
        PdfPCell titleCell = CellFactory.getSimpleTitleCell(title);
        table.addCell(titleCell);
        return table;
    }
}
