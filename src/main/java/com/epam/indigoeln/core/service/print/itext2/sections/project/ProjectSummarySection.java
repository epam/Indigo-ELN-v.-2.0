package com.epam.indigoeln.core.service.print.itext2.sections.project;

import com.epam.indigoeln.core.service.print.itext2.model.project.ProjectSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;

public class ProjectSummarySection extends BasePdfSectionWithSimpleTitle<ProjectSummaryModel> {
    public ProjectSummarySection(ProjectSummaryModel model) {
        super(model, "SUMMARY");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(new float[]{1, 4}, width);
        PdfPTableHelper helper = new PdfPTableHelper(table);
        helper.addKeyValueCells("Project keywords", model.getKeywords());
        helper.addKeyValueCells("Literature", model.getLiterature());
        helper.addKeyValueCells("Project Description", model.getDescription());
        return table;
    }
}
