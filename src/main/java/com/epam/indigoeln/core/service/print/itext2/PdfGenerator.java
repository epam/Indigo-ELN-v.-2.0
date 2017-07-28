package com.epam.indigoeln.core.service.print.itext2;

import com.epam.indigoeln.core.service.print.itext2.model.BaseHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.sections.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfLayout;
import com.epam.indigoeln.core.service.print.itext2.utils.PdfPostProcessor;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import java.io.*;
import java.util.List;

/**
 * This class is used for generating a report.
 * A report consist of a header, which is the same for all pages and a list of pdf sections. <br>
 * <br>
 * To create new section you need to: <br>
 * 1) Create section model class in model package. the bean class must implement SectionModel interface <br>
 * 2) Create pdf section class, parametrized with the model class. <br>
 * 3) Make you sectionsProvider return new section. <br>
 * <br>
 * PdfGenerator abstracts away from sections implementation.<br>
 * To create new report type you just need to create new {@link PdfSectionsProvider}<br>
 */
public class PdfGenerator {
    private final List<AbstractPdfSection> contentSections;
    private final PdfLayout layout;
    private final HeaderPdfSection headerSection;

    public PdfGenerator(PdfSectionsProvider sectionsProvider) {
        this.headerSection = sectionsProvider.getHeaderSection();
        this.contentSections = sectionsProvider.getContentSections();
        this.layout = new PdfLayout(PageSize.A4, 50, 35, 33, 33, headerSection);
    }

    public void generate(OutputStream output) {
        try {
            generateImpl(output);
        } catch (DocumentException | IOException e) {
            throw new PdfGeneratorException(e);
        }
    }

    private void generateImpl(OutputStream output) throws DocumentException, IOException {
        Document document = new Document(
                layout.getPageSize(),
                layout.getMarginLeft(),
                layout.getMarginRight(),
                layout.calcContentMarginTop(),
                layout.getMarginBottom()
        );

        ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, tempBaos);
        writer.setPdfVersion(PdfWriter.VERSION_1_7);

        document.open();
        fillDocument(document, writer);
        document.close();

        addDocumentHeaders(output, new ByteArrayInputStream(tempBaos.toByteArray()));
    }

    private void fillDocument(Document document, PdfWriter writer) throws DocumentException {
        float contentWidth = layout.getContentAvailableWidth();
        for (AbstractPdfSection section : contentSections) {
            section.init(contentWidth);
            section.addToDocument(document, writer);
        }
    }

    /**
     * The method adds header to document pages.
     * This is done after the document is built because when rendering the header
     * we must know total amount of pages in the document.
     *
     * @param input  input stream with already built document
     * @param output output stream where to flush final pdf document.
     */
    private void addDocumentHeaders(OutputStream output, InputStream input) throws IOException, DocumentException {
        try (PdfPostProcessor processor = new PdfPostProcessor(output, input, layout)) {
            int totalPages = processor.getReader().getNumberOfPages();

            BaseHeaderModel template = headerSection.getHeaderModelTemplate();
            template.setTotalPages(totalPages);
            for (int currentPage = 1; currentPage <= totalPages; currentPage++) {
                template.setCurrentPage(currentPage);
                headerSection.init(layout.getContentAvailableWidth());
                processor.drawCentralized(headerSection.getElements(), currentPage, layout.getTop());
            }
        }
    }

    private static class PdfGeneratorException extends RuntimeException {
        PdfGeneratorException(Exception cause) {
            super(cause);
        }
    }
}
