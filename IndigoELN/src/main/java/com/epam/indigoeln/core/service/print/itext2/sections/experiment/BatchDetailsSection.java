package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchDetailsModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;

public class BatchDetailsSection extends BasePdfSectionWithSimpleTitle<BatchDetailsModel> {
    public BatchDetailsSection(BatchDetailsModel model) {
        super(model, "BATCH DETAILS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        String[] headers = {"For Notebook Batch: ", model.getFullNbkBatch() + " #barcode"};
        PdfPTable table = TableFactory.createDefaultTable(headers, new float[]{1, 2}, width, Element.ALIGN_LEFT);

        model.getData().forEach((key, value) -> {
            table.addCell(CellFactory.getCommonCell(key));
            table.addCell(CellFactory.getCommonCell(value));
        });

        return table;
    }
}
