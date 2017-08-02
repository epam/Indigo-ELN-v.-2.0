package com.epam.indigoeln.core.service.print.itext2.sections.notebook;

import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.lowagie.text.pdf.PdfPTable;

public class NotebookSummarySection extends BasePdfSectionWithSimpleTitle<NotebookSummaryModel> {
    public NotebookSummarySection(NotebookSummaryModel model) {
        super(model, "SUMMARY");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        return null;
    }
}
