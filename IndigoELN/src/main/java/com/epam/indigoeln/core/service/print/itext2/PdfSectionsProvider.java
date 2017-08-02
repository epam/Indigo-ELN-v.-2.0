package com.epam.indigoeln.core.service.print.itext2;

import com.epam.indigoeln.core.service.print.itext2.sections.HeaderPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.AbstractPdfSection;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for providing a list of pdf content sections and pdf header section for report generator.
 * PdfGenerator abstracts away from sections implementation.
 * You can create you own providers for custom types of report.
 */
public interface PdfSectionsProvider {
    /**
     * @return list of raw uninitialized pdf sections
     */
    List<AbstractPdfSection> getContentSections();

    /**
     * @return raw uninitialized header section
     */
    HeaderPdfSection getHeaderSection();

    /**
     * @return streams with additional information
     */
    default List<InputStream> getExtraPdf(){
        return new ArrayList<>(1);
    }
}
