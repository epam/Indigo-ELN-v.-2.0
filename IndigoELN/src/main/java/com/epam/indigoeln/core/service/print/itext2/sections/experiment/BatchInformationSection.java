package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
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
    private static final float[] columnWidth = new float[]{1, 3, 1, 1, 1, 3};

    private static final float[] infoColumnWidth = new float[]{2, 3};

    private static final float CELL_VERTICAL_PADDING = 4;

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

        float yieldPart = (float) (columnWidth[3] / DoubleStreamEx.of(columnWidth).sum());
        float yieldWidth = yieldPart * width;

        for (BatchInformationRow row : model.getRows()) {
            Structure structure = row.getStructure();

            PdfPTable structureTable = TableFactory.createDefaultTable(1, structureTableWidth);

            if (structure.getImage().getPngBytes(structureTableWidth).isPresent()){
                PdfPCell imageCell = CellFactory.getImageCell(structure.getImage(), structureTableWidth);
                structureTable.addCell(alignCenterWithoutBorder(imageCell));
            }
            String content = structure.getName() + " " + structure.getDescription();
            if (!StringUtils.isBlank(content)) {
                PdfPCell textCell = CellFactory.getCommonCell(content);
                structureTable.addCell(alignCenterWithoutBorder(textCell));
            }

      //TODO      structureTable.getRow(0).setMaxHeights(imageCell.getImage().getScaledHeight());

            PdfPTable yieldTable = TableFactory.createDefaultTable(1, yieldWidth);

            if (!StringUtils.isBlank(row.getTheoWeight())){
                yieldTable.addCell(yieldCell(FormatUtils.formatDecimal(row.getTheoWeight(), row.getTheoWeightUnit())));
            }
            if (!StringUtils.isBlank(row.getYield())){
                yieldTable.addCell(yieldCell(FormatUtils.formatDecimal(row.getYield(), "%")));
            }

            PdfPTable batchInformation = TableFactory.createDefaultTable(infoColumnWidth, infoWidth);
            BatchInformation batchInfo = row.getBatchInformation();

            PdfPCell molWeightLabel = batchCell("Mol Wgt:");
            PdfPCell molWeight = batchCell(FormatUtils.formatDecimal(batchInfo.getMolWeight()));
            PdfPCell exactMassLabel = batchCell("Exact Mass:");
            PdfPCell exactMass = batchCell(FormatUtils.formatDecimal(batchInfo.getExactMass()));
            PdfPCell saltCodeLabel = batchCell("Salt Code:");
            PdfPCell saltCode = batchCell(batchInfo.getSaltCode());
            PdfPCell saltEqLabel = batchCell("Salt EQ:");
            PdfPCell saltEq = batchCell(FormatUtils.formatDecimal(batchInfo.getSaltEq()));
            PdfPCell batchOwnerLabel = batchCell("Batch Owner:");
            PdfPCell batchOwner = batchCell( StringUtils.join(batchInfo.getBatchOwner(),COMMA));
            PdfPCell commentsLabel = batchCell("Comments:");
            PdfPCell comments = batchCell(batchInfo.getComments());

            batchInformation.addCell(molWeightLabel);
            batchInformation.addCell(molWeight);
            batchInformation.addCell(exactMassLabel);
            batchInformation.addCell(exactMass);
            batchInformation.addCell(saltCodeLabel);
            batchInformation.addCell(saltCode);
            batchInformation.addCell(saltEqLabel);
            batchInformation.addCell(saltEq);
            batchInformation.addCell(batchOwnerLabel);
            batchInformation.addCell(batchOwner);
            batchInformation.addCell(commentsLabel);
            batchInformation.addCell(comments);

            table.addCell(CellFactory.getCommonCell(row.getNbkBatch()));
            table.addCell(CellFactory.getCommonCell(structureTable));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getAmountMade(), row.getAmountMadeUnit())));
            table.addCell(CellFactory.getCommonCell(yieldTable));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getPurity())));
            table.addCell(CellFactory.getCommonCell(batchInformation, CELL_VERTICAL_PADDING));
        }

        return table;
    }

    private PdfPCell alignCenterWithoutBorder(PdfPCell cell) {
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell batchCell(String content){
        PdfPCell commonCell = CellFactory.getCommonCell(content);
        commonCell.setBorder(Rectangle.NO_BORDER);
        commonCell.setPaddingTop(CELL_VERTICAL_PADDING);
        commonCell.setPaddingBottom(CELL_VERTICAL_PADDING);
        return commonCell;
    }

    private PdfPCell yieldCell(String content){
        PdfPCell commonCell = CellFactory.getCommonCell(content);
        commonCell.setBorder(Rectangle.NO_BORDER);
        return commonCell;
    }

}
