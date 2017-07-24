package com.epam.indigoeln.core.service.print.itext2.sections;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class BasePdfSectionWithTableAndTitle<T extends SectionModel> extends AbstractPdfSection<T> {
    private PdfPTable titleTable;
    private PdfPTable contentTable;

    private static final int MIN_SPACE_FOR_CONTENT_ON_PAGE = 40;

    public BasePdfSectionWithTableAndTitle(T model) {
        super(model);
    }

    @Override
    protected List<PdfPTable> generateSectionElements(float width) {
        titleTable = generateTitleTable(width);
        contentTable = generateContentTable(width);
        return StreamEx.of(titleTable, contentTable).filter(Objects::nonNull).toList();
    }

    protected abstract PdfPTable generateTitleTable(float width);

    protected abstract PdfPTable generateContentTable(float width);

    @Override
    public void addToDocument(Document document, PdfWriter writer) throws DocumentException {
        if (!isContentTableFittingPage(document, writer)) {
            document.newPage();
        }
        super.addToDocument(document, writer);
    }

    private boolean isContentTableFittingPage(Document document, PdfWriter writer) {
        float remainingPageHeight = writer.getVerticalPosition(true) - document.bottom();

        float titleTableHeight = Optional.of(titleTable).map(PdfPTable::getTotalHeight).orElse(0f);
        float contentTableHeaderHeight = Optional.of(contentTable).map(PdfPTable::getHeaderHeight).orElse(0f);
        float contentAvailableHeight = remainingPageHeight - titleTableHeight - contentTableHeaderHeight;

        return contentAvailableHeight >= MIN_SPACE_FOR_CONTENT_ON_PAGE;
    }
}
