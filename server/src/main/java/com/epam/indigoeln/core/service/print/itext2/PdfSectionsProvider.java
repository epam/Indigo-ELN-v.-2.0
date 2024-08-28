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
package com.epam.indigoeln.core.service.print.itext2;

import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.HeaderPdfSection;

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
     * @return streams with extra Pdf
     */
    default List<InputStream> getExtraPdf() {
        return new ArrayList<>(1);
    }
}
