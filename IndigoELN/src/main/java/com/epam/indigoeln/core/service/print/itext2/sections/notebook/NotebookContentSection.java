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

import static com.epam.indigoeln.core.service.print.itext2.model.notebook.NotebookContentModel.*;

public class NotebookContentSection extends BasePdfSectionWithSimpleTitle<NotebookContentModel> {

    private static final String[] HEADERS = new String[]{
            "Notebook - Experiment", "Experiment Details", "Reaction Scheme"
    };
    private static final float[] COLUMNS_WIDTH = new float[]{2, 3, 5};

    public NotebookContentSection(NotebookContentModel model) {
        super(model, "NOTEBOOK CONTENT");
    }

    @Override
    protected PdfPTable generateContentTable(float width) {

        PdfPTable table = TableFactory.createDefaultTable(HEADERS, COLUMNS_WIDTH, width);

        float detailsPart = (float) (COLUMNS_WIDTH[1] / DoubleStreamEx.of(COLUMNS_WIDTH).sum());
        float detailsWidth = detailsPart * width;

        float schemePart = (float) (COLUMNS_WIDTH[2] / DoubleStreamEx.of(COLUMNS_WIDTH).sum());
        float schemeWidth = schemePart * width;

        for (ContentModelRow row : model.getContentModelRows()) {

            Details details = row.getDetails();

            PdfPTable detailsTable = TableFactory.createDefaultTable(2, detailsWidth);


            PdfPCell dateLabel = getDetailsCell("Creation date:");
            PdfPCell date = getDetailsCell(FormatUtils.format(details.getCreationDate()));
            PdfPCell titleLabel = getDetailsCell("Subject/Title:");
            PdfPCell title = getDetailsCell(details.getTitle());
            PdfPCell authorLabel = getDetailsCell("Author:");
            PdfPCell author = getDetailsCell(details.getAuthor());
            PdfPCell statusLabel = getDetailsCell("Experiment Status:");
            PdfPCell status = getDetailsCell(details.getStatus());

            detailsTable.addCell(dateLabel);
            detailsTable.addCell(date);
            detailsTable.addCell(titleLabel);
            detailsTable.addCell(title);
            detailsTable.addCell(authorLabel);
            detailsTable.addCell(author);
            detailsTable.addCell(statusLabel);
            detailsTable.addCell(status);

            table.addCell(CellFactory.getCommonCell(row.getNotebookExperiment()));
            table.addCell(CellFactory.getCommonCell(detailsTable));
            table.addCell(CellFactory.getImageCell(row.getReactionScheme(), schemeWidth));
        }
        return table;
    }

    private static PdfPCell getDetailsCell(String content) {
        PdfPCell commonCell = CellFactory.getCommonCell(content);
        commonCell.setBorder(Rectangle.NO_BORDER);
        return commonCell;
    }
}
