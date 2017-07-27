package com.epam.indigoeln.core.service.print.itext2.utils;

import com.epam.indigoeln.core.service.print.itext2.model.image.PdfImage;
import com.epam.indigoeln.core.service.registration.RegistrationService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Optional;

import static com.epam.indigoeln.core.service.print.itext2.utils.PdfConst.FONT_FAMILY;

public class CellFactory {
    private static final Color FONT_COLOR = new Color(100, 100, 100);
    private static final Color CELL_BORDER_COLOR = new Color(170, 170, 170);

    private static final float FONT_SIZE = 6.8f;
    private static final Font FONT = FontFactory.getFont(FONT_FAMILY, BaseFont.IDENTITY_H,true, 8, Font.NORMAL, FONT_COLOR);
    private static final Font FONT_BOLD = FontFactory.getFont(FONT_FAMILY, BaseFont.IDENTITY_H,true, FONT_SIZE, Font.BOLD, FONT_COLOR);

    private static final float CELL_HORIZONTAL_PADDING = 6;
    private static final float CELL_VERTICAL_PADDING = 9;
    private static final float SIMPLE_TITLE_CELL_PADDING = 9;
    private static final float SIMPLE_TITLE_CELL_PADDING_TOP = 20;
    private static final float CELL_BORDER_WIDTH = 0.2f;

    // To avoid cells with too high images
    private static final float IMAGE_MAX_HEIGHT_WIDTH_RATIO = 1f / 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(CellFactory.class);

    private CellFactory() {
    }

    private static PdfPCell withCommonStyle(PdfPCell cellIn) {
        PdfPCell cell = cellIn;
        cell = withCommonPadding(cell);
        cell = withCommonBorder(cell);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setUseBorderPadding(true);
        cell.setUseAscender(true);
        return cell;
    }

    private static PdfPCell withCommonPadding(PdfPCell cell) {
        cell.setPaddingLeft(CELL_HORIZONTAL_PADDING);
        cell.setPaddingRight(CELL_HORIZONTAL_PADDING);
        cell.setPaddingTop(CELL_VERTICAL_PADDING);
        cell.setPaddingBottom(CELL_VERTICAL_PADDING);
        return cell;
    }

    private static PdfPCell withCommonBorder(PdfPCell cell) {
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(CELL_BORDER_WIDTH);
        cell.setBorderColor(CELL_BORDER_COLOR);
        return cell;
    }

    public static PdfPCell getCommonCell(PdfPTable innerTable) {
        PdfPCell cell = new PdfPCell(innerTable);
        return withCommonStyle(cell);
    }

    public static PdfPCell getCommonCell() {
        return withCommonStyle(new PdfPCell());
    }

    public static PdfPCell getCommonCell(String content) {
        return getCommonCell(content, false);
    }

    @SuppressWarnings("unchecked")
    public static PdfPCell getCommonCellWithHtml(String content) {
        PdfPCell pdfPCell = withCommonStyle(new PdfPCell());
        try {
            ArrayList<Element> list = HTMLWorker.parseToList(new StringReader(content), null);
            for (Element element : list) {
                ArrayList<Chunk> chunks = element.getChunks();
                for (Chunk chunk : chunks) {
                    Font oldFont = chunk.getFont();
                    chunk.setFont(FontFactory.getFont(FONT_FAMILY, BaseFont.IDENTITY_H,true,8, oldFont.getStyle(), FONT_COLOR));
                }
                pdfPCell.addElement(element);
            }
        } catch (IOException e) {
            LOGGER.error("Experiment description parse error",e);
        }
        return pdfPCell;
    }

    public static PdfPCell getCommonCell(String content, boolean isBold) {
        Font fontToUse = isBold ? FONT_BOLD : FONT;
        return getCommonCell(content, fontToUse);
    }

    public static PdfPCell getCommonCell(String content, Font font) {
        Phrase phrase = getCommonPhrase(content, font);
        return withCommonStyle(new PdfPCell(phrase));
    }

    public static PdfPCell getImageCell(PdfImage image, float fitWidth) {
        return getImageCell(image, fitWidth, fitWidth * IMAGE_MAX_HEIGHT_WIDTH_RATIO);
    }

    public static PdfPCell getImageCell(PdfImage pdfImage, float fitWidth, float fitHeight) {
        final PdfPCell cell = new PdfPCell();
        Optional<byte[]> bytes = pdfImage.getPngBytes(fitWidth);
        bytes.ifPresent(b -> {
            try {
                Image image = Image.getInstance(b);
                image.scaleToFit(fitWidth, fitHeight);
                cell.setImage(image);
            } catch (IOException | BadElementException e) {
                throw new PdfPCellFactoryException(e);
            }
        });
        return withCommonStyle(cell);
    }

    public static PdfPCell getSimpleTitleCell(String title) {
        PdfPCell cell = getCommonCell(title.toUpperCase());
        cell.setPadding(SIMPLE_TITLE_CELL_PADDING);
        cell.setPaddingTop(SIMPLE_TITLE_CELL_PADDING_TOP);
        cell.setPaddingLeft(0);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    public static Phrase getCommonPhrase(String content) {
        return getCommonPhrase(content, FONT);
    }

    public static Phrase getCommonPhrase(String content, Font font) {
        Phrase phrase = new Phrase(content, font);
        phrase.setLeading(0);
        return phrase;
    }


    public static class PdfPCellFactoryException extends RuntimeException {
        PdfPCellFactoryException(Exception e) {
            super(e);
        }
    }
}
