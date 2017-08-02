package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

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
        private String amountMadeUnit;
        private String theoWeight;
        private String theoWeightUnit;
        private String yield;
        private String purity;
        private BatchInformation batchInformation;

        public String getNbkBatch() {
            return nbkBatch;
        }

        public Structure getStructure() {
            return structure;
        }

        public String getAmountMade() {
            return amountMade;
        }

        public String getYield() {
            return yield;
        }

        public String getPurity() {
            return purity;
        }

        public String getAmountMadeUnit() {
            return amountMadeUnit;
        }

        public String getTheoWeight() {
            return theoWeight;
        }

        public String getTheoWeightUnit() {
            return theoWeightUnit;
        }

        public BatchInformation getBatchInformation() {
            return batchInformation;
        }

        public BatchInformationRow setNbkBatch(String nbkBatch) {
            this.nbkBatch = nbkBatch;
            return this;
        }

        public BatchInformationRow setStructure(Structure structure) {
            this.structure = structure;
            return this;
        }

        public BatchInformationRow setAmountMade(String amountMade) {
            this.amountMade = amountMade;
            return this;
        }

        public BatchInformationRow setAmountMadeUnit(String amountMadeUnit) {
            this.amountMadeUnit = amountMadeUnit;
            return this;
        }

        public BatchInformationRow setTheoWeight(String theoWeight) {
            this.theoWeight = theoWeight;
            return this;
        }

        public BatchInformationRow setTheoWeightUnit(String theoWeightUnit) {
            this.theoWeightUnit = theoWeightUnit;
            return this;
        }

        public BatchInformationRow setYield(String yield) {
            this.yield = yield;
            return this;
        }

        public BatchInformationRow setPurity(String purity) {
            this.purity = purity;
            return this;
        }

        public BatchInformationRow setBatchInformation(BatchInformation batchInformation) {
            this.batchInformation = batchInformation;
            return this;
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

    public static class BatchInformation{
        private String molWeight;
        private String exactMass;
        private String saltCode;
        private String saltEq;
        private List<String> batchOwner;
        private String comments;

        public BatchInformation(String molWeight, String exactMass, String saltCode, String saltEq, List<String> batchOwner, String comments) {
            this.molWeight = molWeight;
            this.exactMass = exactMass;
            this.saltCode = saltCode;
            this.saltEq = saltEq;
            this.batchOwner = batchOwner;
            this.comments = comments;
        }

        public String getMolWeight() {
            return molWeight;
        }

        public String getExactMass() {
            return exactMass;
        }

        public String getSaltCode() {
            return saltCode;
        }

        public String getSaltEq() {
            return saltEq;
        }

        public List<String> getBatchOwner() {
            return batchOwner;
        }

        public String getComments() {
            return comments;
        }
    }
}
