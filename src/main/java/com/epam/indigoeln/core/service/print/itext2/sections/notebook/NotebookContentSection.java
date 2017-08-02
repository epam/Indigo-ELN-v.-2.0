package com.epam.indigoeln.core.service.print.itext2.sections.notebook;

import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.lowagie.text.pdf.PdfPTable;

public class NotebookContentSection extends BasePdfSectionWithSimpleTitle<NotebookContentModel> {

    public NotebookContentSection(NotebookContentModel model) {
        super(model, "NOTEBOOK CONTENT");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        return null;
    }
}
