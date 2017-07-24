package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel.RegistrationSummaryRow;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import one.util.streamex.StreamEx;

public class RegistrationSummarySection extends BasePdfSectionWithSimpleTitle<RegistrationSummaryModel> {
    public RegistrationSummarySection(RegistrationSummaryModel model) {
        super(model, "REGISTRATION SUMMARY");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(new float[]{4, 3, 4, 3}, width);
        for (RegistrationSummaryRow row : model.getRows()) {
            StreamEx.of(
                    CellFactory.getCommonCell(row.getFullNbkBatch()),
                    CellFactory.getCommonCell(row.getTotalAmountMade()),
                    CellFactory.getCommonCell(row.getRegistrationStatus()),
                    CellFactory.getCommonCell(row.getConversationalBatch())
            ).forEach(cell -> {
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            });
        }
        return table;
    }
}
