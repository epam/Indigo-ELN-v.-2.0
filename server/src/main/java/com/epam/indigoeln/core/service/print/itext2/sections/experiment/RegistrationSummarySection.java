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

import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel.RegistrationSummaryRow;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import one.util.streamex.StreamEx;

public class RegistrationSummarySection extends BasePdfSectionWithSimpleTitle<RegistrationSummaryModel> {
    private static final String[] HEADERS = new String[]{
            "Nbk Batch", "Total Amount Made", "Registration Status", "Conversational Batch #"
    };
    private static final float[] COLUMN_WIDTH = new float[]{3, 3, 3, 4};

    public RegistrationSummarySection(RegistrationSummaryModel model) {
        super(model, "REGISTRATION SUMMARY");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(HEADERS, COLUMN_WIDTH, width);
        for (RegistrationSummaryRow row : model.getRows()) {
            StreamEx.of(
                    CellFactory.getCommonCell(row.getFullNbkBatch()),
                    CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getTotalAmountMade(),
                            row.getTotalAmountMadeUnit())),
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
