package com.epam.indigoeln.core.service.print.itext2;

import com.epam.indigoeln.core.service.print.itext2.sections.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.AbstractPdfSection;

import java.util.List;

/**
 * This interface is responsible for providing list of pdf content sections and pdf header section for report generator.
 * PdfGenerator abstracts away from sections implementation.
 * You can create you own providers for custom types of report.
 */
public interface PdfSectionsProvider {
    List<AbstractPdfSection> getContentSections();

    HeaderPdfSection getHeaderSection();
}
