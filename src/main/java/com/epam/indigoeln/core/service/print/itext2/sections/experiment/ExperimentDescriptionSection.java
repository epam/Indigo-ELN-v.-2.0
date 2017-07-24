package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.ExperimentDescriptionModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.pdf.PdfPTable;

public class ExperimentDescriptionSection extends BasePdfSectionWithSimpleTitle<ExperimentDescriptionModel> {
    public ExperimentDescriptionSection(ExperimentDescriptionModel model) {
        super(model, "EXPERIMENT DESCRIPTION");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(1, width);
        table.addCell(CellFactory.getCommonCell(model.getDescription()
                + "\nTODO: parse HTML"
                + "\nTODO: support Russian text"
        ));
        return table;
    }
}
