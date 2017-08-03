package com.epam.indigoeln.core.service.print.itext2.sections.common;

import com.epam.indigoeln.core.service.print.itext2.model.common.BaseHeaderModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.*;

import static com.epam.indigoeln.core.service.print.itext2.utils.PdfConst.BOLD_FONT_FAMILY;

public abstract class BaseHeaderSectionWithLogo<T extends BaseHeaderModel>
        extends BasePdfSectionWithTableAndTitle<T>
        implements HeaderPdfSection {

    private static final String CONFIDENTIAL = "CONFIDENTIAL";
    private static final Font CONFIDENTIAL_FONT = FontFactory.getFont(
            BOLD_FONT_FAMILY, BaseFont.IDENTITY_H, true,14, Font.ITALIC | Font.BOLD, new Color(0x4252af)
    );
    private static final float FIRST_ROW_BOTTOM_PADDING = 15;
    private static final float HEADER_TITLE_HEIGHT = 50f;
    private static final float HEADER_TITLE_WIDTH = 158f;

    public BaseHeaderSectionWithLogo(T model) {
        super(model);
    }

    @Override
    protected PdfPTable generateTitleTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(2, width);

        PdfPCell logoCell = buildLogoCell(model.getLogo(), HEADER_TITLE_WIDTH, HEADER_TITLE_HEIGHT);
        PdfPCell confCell = buildConfidentialCell(HEADER_TITLE_HEIGHT);
        table.addCell(logoCell);
        table.addCell(confCell);

        table.getRow(0).setMaxHeights(logoCell.getFixedHeight());
        table.calculateHeights(false);

        return table;
    }

    private PdfPCell buildLogoCell(PdfImage logo, float width, float rowHeight) {
        final PdfPCell cell = CellFactory.getImageCell(logo, width, rowHeight);
        cell.setFixedHeight(rowHeight + FIRST_ROW_BOTTOM_PADDING);
        cell.setPaddingBottom(FIRST_ROW_BOTTOM_PADDING);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell buildConfidentialCell(float rowHeight) {
        PdfPCell cell = CellFactory.getCommonCell(CONFIDENTIAL, CONFIDENTIAL_FONT);
        cell.setPaddingTop(rowHeight * 0.5f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    @Override
    public BaseHeaderModel getHeaderModelTemplate() {
        return model;
    }
}
