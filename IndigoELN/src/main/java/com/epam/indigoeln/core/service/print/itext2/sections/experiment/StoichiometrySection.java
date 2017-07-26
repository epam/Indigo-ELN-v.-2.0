package com.epam.indigoeln.core.service.print.itext2.sections.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel.Structure;
import com.epam.indigoeln.core.service.print.itext2.sections.BasePdfSectionWithSimpleTitle;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel;
import com.epam.indigoeln.core.service.print.itext2.utils.CellFactory;
import com.epam.indigoeln.core.service.print.itext2.utils.TableFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import one.util.streamex.DoubleStreamEx;

public class StoichiometrySection extends BasePdfSectionWithSimpleTitle<StoichiometryModel> {
    private static final String SPACE = " ";

    public StoichiometrySection(StoichiometryModel model) {
        super(model, "STOICHIOMETRY");
    }

    private static final String[] headers = new String[]{
            "Reagent", "Mol Wgh", "Weight", "Moles", "Volume", "EQ", "Other Information"
    };
    private static final float[] columnWidth = new float[]{3.5f, 1, 1, 1, 1, 1, 3};

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
            PdfPCell imageCell = CellFactory.getImageCell(structure.getImage(), reagentCellWidth);
            PdfPCell fullNbkBatch = CellFactory.getCommonCell(row.getFullNbkBatch());
            PdfPCell compoundId = CellFactory.getCommonCell(row.getCompoundId());

            reagentTable.addCell(alignCenterWithoutBorder(imageCell));
            reagentTable.addCell(alignCenterWithoutBorder(fullNbkBatch));
            reagentTable.addCell(alignCenterWithoutBorder(compoundId));

            //TODO   reagentTable.getRow(0).setMaxHeights(imageCell.getImage().getScaledHeight());

            PdfPTable otherInformation = TableFactory.createDefaultTable(2, infoWidth);

            PdfPCell chemNameLabel = CellFactory.getCommonCell("Name:");
            PdfPCell chemicalName = CellFactory.getCommonCell(row.getChemicalName());
            PdfPCell rxnRoleLabel = CellFactory.getCommonCell("Rxn Role:");
            PdfPCell rxnRole = CellFactory.getCommonCell(row.getRxnRole());
            PdfPCell purityLabel = CellFactory.getCommonCell("Purity:");
            PdfPCell stoicPurity = CellFactory.getCommonCell(row.getStoicPurity());
            PdfPCell molarityLabel = CellFactory.getCommonCell("Molarity:");
            PdfPCell molarity = CellFactory.getCommonCell(row.getMolarity() + SPACE + row.getMolarityUnit());
            PdfPCell hazardLabel = CellFactory.getCommonCell("Hazard:");
            PdfPCell hazard = CellFactory.getCommonCell(row.getHazardComments());
            PdfPCell commentsLabel = CellFactory.getCommonCell("Comments:");
            PdfPCell comments = CellFactory.getCommonCell(row.getComments());
            PdfPCell saltCodeLabel = CellFactory.getCommonCell("Salt Code:");
            PdfPCell saltCode = CellFactory.getCommonCell(row.getSaltCode());
            PdfPCell saltEqLabel = CellFactory.getCommonCell("Salt EQ:");
            PdfPCell saltEq = CellFactory.getCommonCell(row.getSaltEq());

            otherInformation.addCell(alignCenterWithoutBorder(chemNameLabel));
            otherInformation.addCell(alignCenterWithoutBorder(chemicalName));
            otherInformation.addCell(alignCenterWithoutBorder(rxnRoleLabel));
            otherInformation.addCell(alignCenterWithoutBorder(rxnRole));
            otherInformation.addCell(alignCenterWithoutBorder(purityLabel));
            otherInformation.addCell(alignCenterWithoutBorder(stoicPurity));
            otherInformation.addCell(alignCenterWithoutBorder(molarityLabel));
            otherInformation.addCell(alignCenterWithoutBorder(molarity));
            otherInformation.addCell(alignCenterWithoutBorder(hazardLabel));
            otherInformation.addCell(alignCenterWithoutBorder(hazard));
            otherInformation.addCell(alignCenterWithoutBorder(commentsLabel));
            otherInformation.addCell(alignCenterWithoutBorder(comments));
            otherInformation.addCell(alignCenterWithoutBorder(saltCodeLabel));
            otherInformation.addCell(alignCenterWithoutBorder(saltCode));
            otherInformation.addCell(alignCenterWithoutBorder(saltEqLabel));
            otherInformation.addCell(alignCenterWithoutBorder(saltEq));

            table.addCell(CellFactory.getCommonCell(reagentTable));
            table.addCell(CellFactory.getCommonCell(row.getMolecularWeight()));
            table.addCell(CellFactory.getCommonCell(row.getWeight() + SPACE + row.getWeightUnit()));
            table.addCell(CellFactory.getCommonCell(row.getMoles() + SPACE + row.getMolesUnit()));
            table.addCell(CellFactory.getCommonCell(row.getVolume() + SPACE + row.getVolumeUnit()));
            table.addCell(CellFactory.getCommonCell(row.getEq()));
            table.addCell(CellFactory.getCommonCell(otherInformation));
        }

        return table;
    }

    private PdfPCell alignCenterWithoutBorder(PdfPCell cell) {
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
}
