package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.epam.indigoeln.core.service.print.itext2.model.common.BaseHeaderModel;

public interface HeaderPdfSection extends PdfSection {
    BaseHeaderModel getHeaderModelTemplate();
}
