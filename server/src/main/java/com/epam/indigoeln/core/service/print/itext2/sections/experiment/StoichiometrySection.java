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

import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel.Structure;
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

import static com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel.StoichiometryRow;

/**
 * Extension of BasePdfSectionWithSimpleTitle for stoichiometry.
 */
public class StoichiometrySection extends BasePdfSectionWithSimpleTitle<StoichiometryModel> {

    private static final String[] HEADERS = new String[]{
            "Reagent", "Mol Wgh", "Weight", "Moles", "Volume", "EQ", "Other Information"
    };
    private static final float[] COLUMN_WIDTH = new float[]{3.5f, 1, 1, 1, 1.2f, 0.75f, 3.05f};
    private static final float[] INFO_COLUMN_WIDTH = new float[]{2.15f, 3};

    private static final float CELL_VERTICAL_PADDING = 4;
    private static final float CELL_HORIZONTAL_PADDING = 2;

    public StoichiometrySection(StoichiometryModel model) {
        super(model, "STOICHIOMETRY");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(HEADERS, COLUMN_WIDTH, width);
        float imagePart = (float) (COLUMN_WIDTH[0] / DoubleStreamEx.of(COLUMN_WIDTH).sum());
        float reagentCellWidth = imagePart * width;

        for (StoichiometryRow row : model.getRows()) {
            PdfPTable reagentTable = TableFactory.createDefaultTable(1, reagentCellWidth);

            Structure structure = row.getStructure();
            if (structure.getImage().getPngBytes(reagentCellWidth).isPresent()) {
                PdfPCell imageCell = CellFactory.getImageCell(structure.getImage(), reagentCellWidth);
                reagentTable.addCell(getStructureCell(imageCell));
            }
            if (!StringUtils.isBlank(row.getFullNbkBatch())) {
                PdfPCell fullNbkBatch = CellFactory.getCommonCell(row.getFullNbkBatch());
                reagentTable.addCell(getStructureCell(fullNbkBatch));
            }
            if (!StringUtils.isBlank(row.getFullNbkBatch())) {
                PdfPCell compoundId = CellFactory.getCommonCell(row.getCompoundId());
                reagentTable.addCell(getStructureCell(compoundId));
            }

            PdfPTable otherInformation = new PdfPTable(INFO_COLUMN_WIDTH);

            PdfPCell chemNameLabel = getInfoCell("Name:");
            PdfPCell chemicalName = getInfoCell(row.getChemicalName());
            PdfPCell rxnRoleLabel = getInfoCell("Rxn Role:");
            PdfPCell rxnRole = getInfoCell(row.getRxnRole());
            PdfPCell purityLabel = getInfoCell("Purity:");
            PdfPCell stoicPurity = getInfoCell(FormatUtils.formatDecimal(row.getStoicPurity()));
            PdfPCell molarityLabel = getInfoCell("Molarity:");
            PdfPCell molarity = getInfoCell(FormatUtils.formatDecimal(row.getMolarity(), row.getMolarityUnit()));
            PdfPCell hazardLabel = getInfoCell("Hazard:");
            PdfPCell hazard = getInfoCell(row.getHazardComments());
            PdfPCell commentsLabel = getInfoCell("Comments:");
            PdfPCell comments = getInfoCell(row.getComments());
            PdfPCell saltCodeLabel = getInfoCell("Salt Code:");
            PdfPCell saltCode = getInfoCell(row.getSaltCode());
            PdfPCell saltEqLabel = getInfoCell("Salt EQ:");
            PdfPCell saltEq = getInfoCell(FormatUtils.formatDecimal(row.getSaltEq()));

            otherInformation.addCell(chemNameLabel);
            otherInformation.addCell(chemicalName);
            otherInformation.addCell(rxnRoleLabel);
            otherInformation.addCell(rxnRole);
            otherInformation.addCell(purityLabel);
            otherInformation.addCell(stoicPurity);
            otherInformation.addCell(molarityLabel);
            otherInformation.addCell(molarity);
            otherInformation.addCell(hazardLabel);
            otherInformation.addCell(hazard);
            otherInformation.addCell(commentsLabel);
            otherInformation.addCell(comments);
            otherInformation.addCell(saltCodeLabel);
            otherInformation.addCell(saltCode);
            otherInformation.addCell(saltEqLabel);
            otherInformation.addCell(saltEq);

            table.addCell(CellFactory.getCommonCell(reagentTable, CELL_VERTICAL_PADDING));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getMolecularWeight())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getWeight(), row.getWeightUnit())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getMoles(), row.getMolesUnit())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getVolume(), row.getVolumeUnit())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getEq())));
            table.addCell(CellFactory.getCommonCell(otherInformation, CELL_VERTICAL_PADDING, CELL_HORIZONTAL_PADDING));
        }

        return table;
    }

    private PdfPCell getStructureCell(PdfPCell cell) {
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(CELL_VERTICAL_PADDING);
        cell.setPaddingBottom(CELL_VERTICAL_PADDING);
        return cell;
    }

    private PdfPCell getInfoCell(String content) {
        PdfPCell commonCell = CellFactory.getCommonCell(content);
        commonCell.setBorder(Rectangle.NO_BORDER);
        commonCell.setPaddingLeft(CELL_HORIZONTAL_PADDING);
        commonCell.setPaddingRight(CELL_HORIZONTAL_PADDING);
        commonCell.setPaddingTop(CELL_VERTICAL_PADDING);
        commonCell.setPaddingBottom(CELL_VERTICAL_PADDING);
        return commonCell;
    }
}
