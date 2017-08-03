package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.common.BaseHeaderSectionWithLogo;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.ExperimentHeaderModel;
import com.lowagie.text.pdf.PdfPTable;

public class ExperimentHeaderSection extends BaseHeaderSectionWithLogo <ExperimentHeaderModel> {

    private static final float[] COLUMNS_WIDTH = new float[]{1, 1, 1, 1.15f};

    public ExperimentHeaderSection(ExperimentHeaderModel model) {
        super(model);
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(COLUMNS_WIDTH, width);

        PdfPTableHelper helper = new PdfPTableHelper(table);
        helper.addKeyValueCells("Project Name", model.getProjectName())
                .addKeyValueCells("Notebook Experiment", model.getNotebookExperiment())
                .addKeyValueCells("Author", model.getAuthor())
                .addKeyValueCells("Status", model.getStatus())
                .addKeyValueCells("Printed Page", model.getCurrentPage() + " of " + model.getTotalPages())
                .addKeyValueCells("Printed Date", FormatUtils.formatSafe(model.getPrintDate()))
                .addKeyValueCells("Subject/Title", model.getTitle(), 3);

        return table;
    }
}
