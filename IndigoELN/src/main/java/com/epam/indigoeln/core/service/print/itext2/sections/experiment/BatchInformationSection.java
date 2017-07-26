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
import org.apache.commons.lang3.StringUtils;
import static com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel.*;

public class BatchInformationSection extends BasePdfSectionWithSimpleTitle<BatchInformationModel> {
    public static final String SPACE = " ";
    public static final String COMMA = ", ";

    private static final String[] headers = new String[]{
            "Nbk Batch", "Structure", "Amount\nMade", "Theoretical\nYield",
            "Purity (%)\nDetermined By", "Batch Information"
    };
    private static final float[] columnWidth = new float[]{1, 2, 1, 1, 1, 4};

    public BatchInformationSection(BatchInformationModel model) {
        super(model, "BATCH INFORMATION");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(headers, columnWidth, width);
        float imagePart = (float) (columnWidth[1] / DoubleStreamEx.of(columnWidth).sum());
        float structureTableWidth = imagePart * width;

        float infoPart = (float) (columnWidth[5] / DoubleStreamEx.of(columnWidth).sum());
        float infoWidth = infoPart * width;

        for (BatchInformationRow row : model.getRows()) {
            Structure structure = row.getStructure();

            PdfPTable structureTable = TableFactory.createDefaultTable(1, structureTableWidth);

            PdfPCell imageCell = CellFactory.getImageCell(structure.getImage(), structureTableWidth);
            PdfPCell textCell = CellFactory.getCommonCell(structure.getName() + " " + structure.getDescription());

            structureTable.addCell(alignCenterWithoutBorder(imageCell));
            structureTable.addCell(alignCenterWithoutBorder(textCell));

      //TODO      structureTable.getRow(0).setMaxHeights(imageCell.getImage().getScaledHeight());

            PdfPTable batchInformation = TableFactory.createDefaultTable(2, infoWidth);
            BatchInformation batchInfo = row.getBatchInformation();

            PdfPCell molWeightLabel = CellFactory.getCommonCell("Mol Wgt:");
            PdfPCell molWeight = CellFactory.getCommonCell(batchInfo.getMolWeight());
            PdfPCell exactMassLabel = CellFactory.getCommonCell("Exact Mass:");
            PdfPCell exactMass = CellFactory.getCommonCell(batchInfo.getExactMass());
            PdfPCell saltCodeLabel = CellFactory.getCommonCell("Salt Code:");
            PdfPCell saltCode = CellFactory.getCommonCell(batchInfo.getSaltCode());
            PdfPCell saltEqLabel = CellFactory.getCommonCell("Salt EQ:");
            PdfPCell saltEq = CellFactory.getCommonCell(batchInfo.getSaltEq());
            PdfPCell batchOwnerLabel = CellFactory.getCommonCell("Batch Owner:");
            PdfPCell batchOwner = CellFactory.getCommonCell( StringUtils.join(batchInfo.getBatchOwner(),COMMA));
            PdfPCell commentsLabel = CellFactory.getCommonCell("Comments:");
            PdfPCell comments = CellFactory.getCommonCell(batchInfo.getComments());

            batchInformation.addCell(alignCenterWithoutBorder(molWeightLabel));
            batchInformation.addCell(alignCenterWithoutBorder(molWeight));
            batchInformation.addCell(alignCenterWithoutBorder(exactMassLabel));
            batchInformation.addCell(alignCenterWithoutBorder(exactMass));
            batchInformation.addCell(alignCenterWithoutBorder(saltCodeLabel));
            batchInformation.addCell(alignCenterWithoutBorder(saltCode));
            batchInformation.addCell(alignCenterWithoutBorder(saltEqLabel));
            batchInformation.addCell(alignCenterWithoutBorder(saltEq));
            batchInformation.addCell(alignCenterWithoutBorder(batchOwnerLabel));
            batchInformation.addCell(alignCenterWithoutBorder(batchOwner));
            batchInformation.addCell(alignCenterWithoutBorder(commentsLabel));
            batchInformation.addCell(alignCenterWithoutBorder(comments));

            table.addCell(CellFactory.getCommonCell(row.getNbkBatch()));
            table.addCell(CellFactory.getCommonCell(structureTable));
            table.addCell(CellFactory.getCommonCell(row.getAmountMade() + SPACE + row.getAmountMadeUnit()));
            table.addCell(CellFactory.getCommonCell(row.getTheoWeight() + SPACE + row.getTheoWeightUnit() +
                                                    System.lineSeparator() + row.getYield() + "%"));
            table.addCell(CellFactory.getCommonCell(row.getPurity()));
            table.addCell(CellFactory.getCommonCell(batchInformation));
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
