package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel.RegistrationSummaryRow;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import one.util.streamex.StreamEx;

public class RegistrationSummarySection extends BasePdfSectionWithSimpleTitle<RegistrationSummaryModel> {
    private static final String[] headers = new String[]{
            "Nbk Batch", "Total Amount Made", "Registration Status", "Conversational Batch #"
    };
    private static final float[] columnWidth = new float[]{3, 3, 3, 4};

    public RegistrationSummarySection(RegistrationSummaryModel model) {
        super(model, "REGISTRATION SUMMARY");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(headers, columnWidth, width);
        for (RegistrationSummaryRow row : model.getRows()) {
            StreamEx.of(
                    CellFactory.getCommonCell(row.getFullNbkBatch()),
                    CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getTotalAmountMade(), row.getTotalAmountMadeUnit())),
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
