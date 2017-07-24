package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import one.util.streamex.DoubleStreamEx;

public class BatchInformationSection extends BasePdfSectionWithSimpleTitle<BatchInformationModel> {
    public BatchInformationSection(BatchInformationModel model) {
        super(model, "BATCH INFORMATION");
    }

    private static final String[] headers = new String[]{
            "Nbk Batch", "Structure", "Amount\nMade", "Theoretical\nYield",
            "Purity (%)\nDetermined By", "Batch Information"
    };
    private static final float[] columnWidth = new float[]{1, 2, 1, 1, 1, 4};

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(headers, columnWidth, width);
        float imagePart = (float) (columnWidth[1] / DoubleStreamEx.of(columnWidth).sum());
        float structureTableWidth = imagePart * width;

        for (BatchInformationModel.BatchInformationRow row : model.getRows()) {
            BatchInformationModel.Structure structure = row.getStructure();
            PdfPTable structureTable = TableFactory.createDefaultTable(1, structureTableWidth);
            PdfPCell imageCell = CellFactory.getImageCell(structure.getImage(), structureTableWidth);
            PdfPCell textCell = CellFactory.getCommonCell(structure.getName() + " " + structure.getDescription());
            structureTable.addCell(alignCenterWithoutBorder(imageCell));
            structureTable.addCell(alignCenterWithoutBorder(textCell));
            structureTable.getRow(0).setMaxHeights(imageCell.getImage().getScaledHeight());

            table.addCell(CellFactory.getCommonCell(row.getNbkBatch()));
            table.addCell(CellFactory.getCommonCell(structureTable));
            table.addCell(CellFactory.getCommonCell(row.getAmountMade()));
            table.addCell(CellFactory.getCommonCell(row.getTheoreticalYield()));
            table.addCell(CellFactory.getCommonCell(row.getPurity()));
            table.addCell(CellFactory.getCommonCell(row.getBatchInformation()));
        }

        return table;
    }

    private PdfPCell alignCenterWithoutBorder(PdfPCell cell) {
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

}
