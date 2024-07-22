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

import com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel;
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

import static com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel.PreferredCompoundsRow;
import static com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel.Structure;

/**
 * Extension of BasePdfSectionWithSimpleTitle for preferred compounds.
 */
public class PreferredCompoundsSection extends BasePdfSectionWithSimpleTitle<PreferredCompoundsModel> {
    private static final String[] HEADERS = new String[]{
            "Structure", "Notebook Batch #", "Mol Weight", "Mol Formula", "Structure Comments"
    };

    private static final float[] COLUMN_WIDTH = new float[]{3, 2, 1, 1.5f, 2};

    private static final float CELL_VERTICAL_PADDING = 4;

    public PreferredCompoundsSection(PreferredCompoundsModel model) {
        super(model, "PREFERRED COMPOUNDS");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(HEADERS, COLUMN_WIDTH, width);

        float structurePart = (float) (COLUMN_WIDTH[0] / DoubleStreamEx.of(COLUMN_WIDTH).sum());
        float structureWidth = structurePart * width;

        for (PreferredCompoundsRow row : model.getRows()) {
            PdfPTable structureTable = TableFactory.createDefaultTable(1, structureWidth);

            Structure structure = row.getStructure();

            if (structure.getImage().getPngBytes(structureWidth).isPresent()) {
                PdfPCell imageCell = CellFactory.getImageCell(structure.getImage(), structureWidth);
                structureTable.addCell(getStructureCell(imageCell));
            }
            if (!StringUtils.isBlank(structure.getVirtualCompoundId())) {
                PdfPCell virtualCompoundId = CellFactory.getCommonCell(structure.getVirtualCompoundId());
                structureTable.addCell(getStructureCell(virtualCompoundId));
            }
            String stereoisomer = structure.getName() + " " + structure.getDescription();
            if (!StringUtils.isBlank(stereoisomer)) {
                PdfPCell stereoisomerCell = CellFactory.getCommonCell(stereoisomer);
                structureTable.addCell(getStructureCell(stereoisomerCell));
            }

            table.addCell(CellFactory.getCommonCell(structureTable, CELL_VERTICAL_PADDING));
            table.addCell(CellFactory.getCommonCell(row.getNotebookBatchNumber()));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getMolWeight())));
            table.addCell(CellFactory.getCommonCell(row.getMolFormula()));
            table.addCell(CellFactory.getCommonCell(row.getStructureComments()));
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
}
