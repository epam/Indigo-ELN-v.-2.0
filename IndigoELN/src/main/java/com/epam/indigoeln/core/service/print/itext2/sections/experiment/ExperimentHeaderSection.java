package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BaseHeaderSectionWithLogo;
import com.epam.indigoeln.core.service.print.itext2.utils.DateTimeUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPTableHelper;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.ExperimentHeaderModel;
import com.lowagie.text.pdf.PdfPTable;

public class ExperimentHeaderSection
        extends BaseHeaderSectionWithLogo <ExperimentHeaderModel> {

    public ExperimentHeaderSection(ExperimentHeaderModel model) {
        super(model);
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(4, width);

        PdfPTableHelper helper = new PdfPTableHelper(table);
        helper.addKeyValueCells("Author", model.getAuthor())
                .addKeyValueCells("Notebook Experiment", model.getNotebookExperiment())
                .addKeyValueCells("Project", model.getProjectName())
                .addKeyValueCells("Status", model.getStatus())
                .addKeyValueCells("Printed Page", model.getCurrentPage() + " of " + model.getTotalPages())
                .addKeyValueCells("Printed Date", DateTimeUtils.formatSafe(model.getPrintDate()))
                .addKeyValueCells("Subject/Title", model.getTitle(), 3);

        return table;
    }
}
