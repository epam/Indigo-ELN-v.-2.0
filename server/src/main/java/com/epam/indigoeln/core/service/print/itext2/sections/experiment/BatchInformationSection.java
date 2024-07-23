/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
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

/**
 * Extension of BasePdfSectionWithSimpleTitle for batch information section.
 */
public class BatchInformationSection extends BasePdfSectionWithSimpleTitle<BatchInformationModel> {
    public static final String SPACE = " ";
    private static final String COMMA = ", ";

    private static final String[] HEADERS = new String[]{
            "Nbk Batch", "Structure", "Amount\nMade", "Theoretical\nYield",
            "Purity (%)\nDetermined By", "Batch Information"
    };
    private static final float[] COLUMN_WIDTH = new float[]{1, 3, 1, 1, 1, 3};

    private static final float[] INFO_COLUMN_WIDTH = new float[]{2, 3};

    private static final float CELL_VERTICAL_PADDING = 4;

    public BatchInformationSection(BatchInformationModel model) {
        super(model, "BATCH INFORMATION");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(HEADERS, COLUMN_WIDTH, width);
        float imagePart = (float) (COLUMN_WIDTH[1] / DoubleStreamEx.of(COLUMN_WIDTH).sum());
        float structureTableWidth = imagePart * width;

        float yieldPart = (float) (COLUMN_WIDTH[3] / DoubleStreamEx.of(COLUMN_WIDTH).sum());
        float yieldWidth = yieldPart * width;

        for (BatchInformationRow row : model.getRows()) {
            Structure structure = row.getStructure();

            PdfPTable structureTable = TableFactory.createDefaultTable(1, structureTableWidth);

            if (structure.getImage().getPngBytes(structureTableWidth).isPresent()) {
                PdfPCell imageCell = CellFactory.getImageCell(structure.getImage(), structureTableWidth);
                structureTable.addCell(getStructureCell(imageCell));
            }
            String content = structure.getName() + " " + structure.getDescription();
            if (!StringUtils.isBlank(content)) {
                PdfPCell textCell = CellFactory.getCommonCell(content);
                structureTable.addCell(getStructureCell(textCell));
            }

            PdfPTable yieldTable = TableFactory.createDefaultTable(1, yieldWidth);

            if (!StringUtils.isBlank(row.getTheoWeight())) {
                yieldTable.addCell(getYieldCell(FormatUtils
                        .formatDecimal(row.getTheoWeight(), row.getTheoWeightUnit())));
            }
            if (!StringUtils.isBlank(row.getYield())) {
                yieldTable.addCell(getYieldCell(FormatUtils.formatDecimal(row.getYield(), "%")));
            }

            PdfPTable batchInformation = new PdfPTable(INFO_COLUMN_WIDTH);
            BatchInformation batchInfo = row.getBatchInformation();

            PdfPCell molWeightLabel = getBatchCell("Mol Wgt:");
            PdfPCell molWeight = getBatchCell(FormatUtils.formatDecimal(batchInfo.getMolWeight()));
            PdfPCell exactMassLabel = getBatchCell("Exact Mass:");
            PdfPCell exactMass = getBatchCell(FormatUtils.formatDecimal(batchInfo.getExactMass()));
            PdfPCell saltCodeLabel = getBatchCell("Salt Code:");
            PdfPCell saltCode = getBatchCell(batchInfo.getSaltCode());
            PdfPCell saltEqLabel = getBatchCell("Salt EQ:");
            PdfPCell saltEq = getBatchCell(FormatUtils.formatDecimal(batchInfo.getSaltEq()));
            PdfPCell batchOwnerLabel = getBatchCell("Batch Owner:");
            PdfPCell batchOwner = getBatchCell(StringUtils.join(batchInfo.getBatchOwner(), COMMA));
            PdfPCell commentsLabel = getBatchCell("Comments:");
            PdfPCell comments = getBatchCell(batchInfo.getComments());

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
            table.addCell(CellFactory.getCommonCell(FormatUtils
                    .formatDecimal(row.getAmountMade(), row.getAmountMadeUnit())));
            table.addCell(CellFactory.getCommonCell(yieldTable));
            table.addCell(CellFactory.getCommonCell(row.getPurity()));
            table.addCell(CellFactory.getCommonCell(batchInformation, CELL_VERTICAL_PADDING));
        }

        return table;
    }

    private PdfPCell getStructureCell(PdfPCell cell) {
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell getBatchCell(String content) {
        PdfPCell commonCell = CellFactory.getCommonCell(content);
        commonCell.setBorder(Rectangle.NO_BORDER);
        commonCell.setPaddingTop(CELL_VERTICAL_PADDING);
        commonCell.setPaddingBottom(CELL_VERTICAL_PADDING);
        return commonCell;
    }

    private PdfPCell getYieldCell(String content) {
        PdfPCell commonCell = CellFactory.getCommonCell(content);
        commonCell.setBorder(Rectangle.NO_BORDER);
        return commonCell;
    }

}
