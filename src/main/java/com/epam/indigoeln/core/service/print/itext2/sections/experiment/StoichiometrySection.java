package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel.Structure;
import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.FormatUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import one.util.streamex.DoubleStreamEx;
import org.apache.commons.lang3.StringUtils;

public class StoichiometrySection extends BasePdfSectionWithSimpleTitle<StoichiometryModel> {

    public StoichiometrySection(StoichiometryModel model) {
        super(model, "STOICHIOMETRY");
    }

    private static final String[] headers = new String[]{
            "Reagent", "Mol Wgh", "Weight", "Moles", "Volume", "EQ", "Other Information"
    };
    private static final float[] columnWidth = new float[]{3.5f, 1, 1, 1, 1, 1, 3};
    private static final float[] infoColumnWidth = new float[]{2, 3};

    private static final float CELL_VERTICAL_PADDING = 4;

    @Override
    protected PdfPTable generateContentTable(float width) {
        PdfPTable table = TableFactory.createDefaultTable(headers, columnWidth, width);
        float imagePart = (float) (columnWidth[0] / DoubleStreamEx.of(columnWidth).sum());
        float reagentCellWidth = imagePart * width;

        float infoPart = (float) (columnWidth[6] / DoubleStreamEx.of(columnWidth).sum());
        float infoWidth = infoPart * width;

        for (StoichiometryModel.StoichiometryRow row : model.getRows()) {
            PdfPTable reagentTable = TableFactory.createDefaultTable(1, reagentCellWidth);

            Structure structure = row.getStructure();
            if (structure.getImage().getPngBytes(reagentCellWidth).isPresent()){
                PdfPCell imageCell = CellFactory.getImageCell(structure.getImage(), reagentCellWidth);
                reagentTable.addCell(alignCenterWithoutBorder(imageCell));
            }
            if (!StringUtils.isBlank(row.getFullNbkBatch())){
                PdfPCell fullNbkBatch = CellFactory.getCommonCell(row.getFullNbkBatch());
                reagentTable.addCell(alignCenterWithoutBorder(fullNbkBatch));
            }
            if (!StringUtils.isBlank(row.getFullNbkBatch())){
                PdfPCell compoundId = CellFactory.getCommonCell(row.getCompoundId());
                reagentTable.addCell(alignCenterWithoutBorder(compoundId));
            }
            //TODO   reagentTable.getRow(0).setMaxHeights(imageCell.getImage().getScaledHeight());

            PdfPTable otherInformation = TableFactory.createDefaultTable(infoColumnWidth, infoWidth);

            PdfPCell chemNameLabel = infoCell("Name:");
            PdfPCell chemicalName = infoCell(row.getChemicalName());
            PdfPCell rxnRoleLabel = infoCell("Rxn Role:");
            PdfPCell rxnRole = infoCell(row.getRxnRole());
            PdfPCell purityLabel = infoCell("Purity:");
            PdfPCell stoicPurity = infoCell(FormatUtils.formatDecimal(row.getStoicPurity()));
            PdfPCell molarityLabel = infoCell("Molarity:");
            PdfPCell molarity = infoCell(FormatUtils.formatDecimal(row.getMolarity(), row.getMolarityUnit()));
            PdfPCell hazardLabel = infoCell("Hazard:");
            PdfPCell hazard = infoCell(row.getHazardComments());
            PdfPCell commentsLabel = infoCell("Comments:");
            PdfPCell comments = infoCell(row.getComments());
            PdfPCell saltCodeLabel = infoCell("Salt Code:");
            PdfPCell saltCode = infoCell(row.getSaltCode());
            PdfPCell saltEqLabel = infoCell("Salt EQ:");
            PdfPCell saltEq = infoCell(FormatUtils.formatDecimal(row.getSaltEq()));

            otherInformation.addCell(chemNameLabel);
            otherInformation.addCell(chemicalName);
            otherInformation.addCell(rxnRoleLabel);
            otherInformation.addCell(rxnRole);
            otherInformation.addCell(purityLabel);
            otherInformation.addCell(stoicPurity);
            otherInformation.addCell(molarityLabel);
            otherInformation.addCell(molarity);
            otherInformation.addCell(hazardLabel);
            otherInformation.addCell(hazard);
            otherInformation.addCell(commentsLabel);
            otherInformation.addCell(comments);
            otherInformation.addCell(saltCodeLabel);
            otherInformation.addCell(saltCode);
            otherInformation.addCell(saltEqLabel);
            otherInformation.addCell(saltEq);

            table.addCell(CellFactory.getCommonCell(reagentTable));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getMolecularWeight())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getWeight(), row.getWeightUnit())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getMoles(), row.getMolesUnit())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getVolume(), row.getVolumeUnit())));
            table.addCell(CellFactory.getCommonCell(FormatUtils.formatDecimal(row.getEq())));
            table.addCell(CellFactory.getCommonCell(otherInformation));
        }

        return table;
    }

    private PdfPCell alignCenterWithoutBorder(PdfPCell cell) {
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(CELL_VERTICAL_PADDING);
        cell.setPaddingBottom(CELL_VERTICAL_PADDING);
        return cell;
    }

    private PdfPCell infoCell(String content){
        PdfPCell commonCell = CellFactory.getCommonCell(content);
        commonCell.setBorder(Rectangle.NO_BORDER);
        commonCell.setPaddingTop(CELL_VERTICAL_PADDING);
        commonCell.setPaddingBottom(CELL_VERTICAL_PADDING);
        return commonCell;
    }
}
