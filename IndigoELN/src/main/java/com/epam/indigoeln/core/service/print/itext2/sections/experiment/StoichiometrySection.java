package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import one.util.streamex.DoubleStreamEx;

public class StoichiometrySection extends BasePdfSectionWithSimpleTitle<StoichiometryModel> {
    public StoichiometrySection(StoichiometryModel model) {
        super(model, "STOICHIOMETRY");
    }

    private static final String[] headers = new String[]{
            "Reagent", "Mol Wgh", "Weight", "Moles", "Volume", "EQ", "Other Information"
    };
    private static final float[] columnWidth = new float[]{3.5f, 1, 1, 1, 1, 1, 3};

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(headers, columnWidth, width);
        float imagePart = (float) (columnWidth[0] / DoubleStreamEx.of(columnWidth).sum());
        float reagentCellWidth = imagePart * width;

        for (StoichiometryModel.StoichiometryRow row : model.getRows()) {
            StoichiometryModel.ReagentInfo reagentInfo = row.getReagentInfo();
            PdfPTable reagentTable = TableFactory.createDefaultTable(1, reagentCellWidth);
            PdfPCell imageCell = CellFactory.getImageCell(reagentInfo.getImage(), reagentCellWidth);
            String content = reagentInfo.getFullNbkBatch() + "\n" + reagentInfo.getCompoundId();
            PdfPCell textCell = CellFactory.getCommonCell(content);
            reagentTable.addCell(alignCenterWithoutBorder(imageCell));
            reagentTable.addCell(alignCenterWithoutBorder(textCell));
//            reagentTable.getRow(0).setMaxHeights(imageCell.getImage().getScaledHeight());

            table.addCell(CellFactory.getCommonCell(reagentTable));
            table.addCell(CellFactory.getCommonCell(row.getMolecularWeight()));
            table.addCell(CellFactory.getCommonCell(row.getWeight()));
            table.addCell(CellFactory.getCommonCell(row.getMoles()));
            table.addCell(CellFactory.getCommonCell(row.getVolume()));
            table.addCell(CellFactory.getCommonCell(row.getEq()));
            table.addCell(CellFactory.getCommonCell(row.getOtherInformation()));
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
