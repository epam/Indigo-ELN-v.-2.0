package com.epam.indigoeln.core.service.print.itext2.sections.project;

import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BaseHeaderSectionWithLogo;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;

public class ProjectHeaderSection extends BaseHeaderSectionWithLogo<ProjectHeaderModel> {

    private static final float[] COLUMN_WIDTH = new float[]{1, 1, 1, 1.15f};

    public ProjectHeaderSection(ProjectHeaderModel model) {
        super(model);
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(COLUMN_WIDTH, width);

        PdfPTableHelper helper = new PdfPTableHelper(table);
        helper.addKeyValueCells("Project Name", model.getProjectName(), 3)
                .addKeyValueCells("Author", model.getAuthor())
                .addKeyValueCells("Creation Date", FormatUtils.formatSafe(model.getCreationDate()))
                .addKeyValueCells("Printed Page", model.getCurrentPage() + " of " + model.getTotalPages())
                .addKeyValueCells("Printed Date", FormatUtils.formatSafe(model.getPrintDate()));

        return table;
    }
}
