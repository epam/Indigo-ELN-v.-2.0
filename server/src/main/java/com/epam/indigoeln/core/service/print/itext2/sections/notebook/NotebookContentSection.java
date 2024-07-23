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
package com.epam.indigoeln.core.service.print.itext2.sections.notebook;

import com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel;
import com.epam.indigoeln.core.service.print.itext2.sections.common.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import one.util.streamex.DoubleStreamEx;

import static com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel.ContentModelRow;
import static com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel.Details;

/**
 * Extension of BasePdfSectionWithSimpleTitle for notebook content.
 */
public class NotebookContentSection extends BasePdfSectionWithSimpleTitle<NotebookContentModel> {

    private static final String[] HEADERS = new String[]{
            "Notebook - Experiment", "Experiment Details", "Reaction Scheme"
    };
    private static final float[] COLUMNS_WIDTH = new float[]{2, 3, 5};
    private static final float CELL_VERTICAL_PADDING = 4;
    private static final float CELL_HORIZONTAL_PADDING = 2;

    public NotebookContentSection(NotebookContentModel model) {
        super(model, "NOTEBOOK CONTENT");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {

        PdfPTable table = TableFactory.createDefaultTable(HEADERS, COLUMNS_WIDTH, width);

        float schemePart = (float) (COLUMNS_WIDTH[2] / DoubleStreamEx.of(COLUMNS_WIDTH).sum());
        float schemeWidth = schemePart * width;

        for (ContentModelRow row : model.getContentModelRows()) {

            Details details = row.getDetails();

            PdfPTable detailsTable = new PdfPTable(2);

            PdfPCell dateLabel = getDetailsCell("Creation date:");
            PdfPCell date = getDetailsCell(FormatUtils.format(details.getCreationDate()));
            PdfPCell authorLabel = getDetailsCell("Author:");
            PdfPCell author = getDetailsCell(details.getAuthor());
            PdfPCell statusLabel = getDetailsCell("Experiment Status:");
            PdfPCell status = getDetailsCell(details.getStatus());
            PdfPCell titleLabel = getDetailsCell("Subject/Title:");
            titleLabel.setColspan(2);
            PdfPCell title = getDetailsCell(details.getTitle());
            title.setColspan(2);

            detailsTable.addCell(dateLabel);
            detailsTable.addCell(date);
            detailsTable.addCell(authorLabel);
            detailsTable.addCell(author);
            detailsTable.addCell(statusLabel);
            detailsTable.addCell(status);
            detailsTable.addCell(titleLabel);
            detailsTable.addCell(title);

            table.addCell(CellFactory.getCommonCell(row.getNotebookExperiment()));
            table.addCell(CellFactory.getCommonCell(detailsTable, CELL_VERTICAL_PADDING, CELL_HORIZONTAL_PADDING));
            table.addCell(CellFactory.getImageCell(row.getReactionScheme(), schemeWidth));
        }
        return table;
    }

    private static PdfPCell getDetailsCell(String content) {
        PdfPCell commonCell = CellFactory.getCommonCell(content);
        commonCell.setBorder(Rectangle.NO_BORDER);
        commonCell.setPaddingTop(CELL_VERTICAL_PADDING);
        commonCell.setPaddingBottom(CELL_VERTICAL_PADDING);
        commonCell.setPaddingLeft(CELL_HORIZONTAL_PADDING);
        commonCell.setPaddingRight(CELL_HORIZONTAL_PADDING);
        return commonCell;
    }
}
