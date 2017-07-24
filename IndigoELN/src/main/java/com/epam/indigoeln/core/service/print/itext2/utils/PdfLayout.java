package com.epam.indigoeln.core.service.print.itext2.utils;

import com.epam.indigoeln.core.service.print.itext2.sections.HeaderPdfSection;
import com.lowagie.text.Rectangle;

public class PdfLayout {
    private Rectangle pageSize;
    private float marginTop;
    private float marginBottom;
    private float marginLeft;
    private float marginRight;
    private float headerHeight;

    private static final float HEADER_OFFSET = 30;

    public PdfLayout(Rectangle pageSize,
                     float marginTop, float marginBottom,
                     float marginLeft, float marginRight,
                     HeaderPdfSection headerSection) {
        this.pageSize = pageSize;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;

        // Create dummy header section to calculate content getTop margin.
        // Real sections are generated after all sections are added to document.

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
