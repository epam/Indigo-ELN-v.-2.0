package com.epam.indigoeln.core.service.print.itext2.sections.notebook;

import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BaseHeaderSectionWithLogo;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;

public class NotebookHeaderSection extends BaseHeaderSectionWithLogo<NotebookHeaderModel> {

    public NotebookHeaderSection(NotebookHeaderModel model) {
        super(model);
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(4, width);

        PdfPTableHelper helper = new PdfPTableHelper(table);
        helper.addKeyValueCells("Project Name", model.getProjectName())
                .addKeyValueCells("Notebook Name", model.getNotebookName()).addKeyValueCells("Author", model.getAuthor())
                .addKeyValueCells("Creation Date", FormatUtils.formatSafe(model.getCreationDate()))
                .addKeyValueCells("Printed Page", model.getCurrentPage() + " of " + model.getTotalPages())
                .addKeyValueCells("Printed Date", FormatUtils.formatSafe(model.getPrintDate()));

        return table;
    }
}
