package com.epam.indigoeln.core.service.print.itext2.sections;

import com.epam.indigoeln.core.service.print.itext2.model.BaseHeaderModel;

public interface HeaderPdfSection extends PdfSection {
    BaseHeaderModel getHeaderModelTemplate();
}
