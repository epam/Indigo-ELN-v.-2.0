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
package com.epam.indigoeln.core.service.print.itext2.utils;

import com.epam.indigoeln.core.service.print.itext2.sections.common.HeaderPdfSection;
import com.lowagie.text.Rectangle;

/**
 * Describes format parameters for pdf layout.
 */
public class PdfLayout {
    private Rectangle pageSize;
    private float headerHeight;

    private float marginTop;
    private float marginBottom;
    private float marginLeft;
    private float marginRight;

    private static final float HEADER_OFFSET = 0;

    public PdfLayout(Rectangle pageSize,
                     float marginTop, float marginBottom,
                     float marginLeft, float marginRight,
                     HeaderPdfSection headerSection) {
        this.pageSize = pageSize;

        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;

        headerSection.init(getContentAvailableWidth());
        this.headerHeight = headerSection.calcHeight();
    }

    public Rectangle getPageSize() {
        return pageSize;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public float calcContentMarginTop() {
        return marginTop + headerHeight + HEADER_OFFSET;
    }

    public float getContentAvailableWidth() {
        return pageSize.getWidth() - marginLeft - marginRight;
    }

    public float getTop() {
        return pageSize.getTop(marginTop);
    }
}
