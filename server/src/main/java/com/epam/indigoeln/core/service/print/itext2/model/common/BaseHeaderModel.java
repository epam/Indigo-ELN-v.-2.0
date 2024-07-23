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
package com.epam.indigoeln.core.service.print.itext2.model.common;

import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

/**
 * Abstract representation for base header.
 */
public abstract class BaseHeaderModel implements SectionModel {
    private static final int DEFAULT_PAGE = -1;

    private int currentPage = DEFAULT_PAGE;
    private int totalPages = DEFAULT_PAGE;
    private PdfImage logo;

    public BaseHeaderModel(PdfImage logo) {
        this.logo = logo;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public PdfImage getLogo() {
        return logo;
    }
}
