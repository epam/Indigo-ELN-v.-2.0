package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.image.PdfImage;

import java.util.List;

public class BatchInformationModel implements SectionModel {
    private List<BatchInformationRow> rows;

    public BatchInformationModel(List<BatchInformationRow> rows) {
        this.rows = rows;
    }

    public List<BatchInformationRow> getRows() {
        return rows;
    }

    public static class BatchInformationRow {
        private String nbkBatch;
        private Structure structure;
        private String amountMade;
        private String theoreticalYield;
        private String purity;
        private String batchInformation;

        public BatchInformationRow(String nbkBatch, Structure structure, String amountMade,
                                   String theoreticalYield, String purity, String batchInformation) {
            this.nbkBatch = nbkBatch;
            this.structure = structure;
            this.amountMade = amountMade;
            this.theoreticalYield = theoreticalYield;
            this.purity = purity;
            this.batchInformation = batchInformation;
        }

        public String getNbkBatch() {
            return nbkBatch;
        }

        public Structure getStructure() {
            return structure;
        }

        public String getAmountMade() {
            return amountMade;
        }

        public String getTheoreticalYield() {
            return theoreticalYield;
        }

        public String getPurity() {
            return purity;
        }

        public String getBatchInformation() {
            return batchInformation;
        }
    }

    public static class Structure {
        private PdfImage image;
        private String name;
        private String description;

        public Structure(PdfImage image, String name, String description) {
            this.image = image;
            this.name = name;
            this.description = description;
        }

        public PdfImage getImage() {
            return image;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
