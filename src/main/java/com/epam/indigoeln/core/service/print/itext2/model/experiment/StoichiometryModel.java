package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.image.PdfImage;

import java.util.List;

public class StoichiometryModel implements SectionModel {
    private List<StoichiometryRow> rows;

    public StoichiometryModel(List<StoichiometryRow> rows) {
        this.rows = rows;
    }

    public List<StoichiometryRow> getRows() {
        return rows;
    }

    public static class StoichiometryRow {
        private String fullNbkBatch;
        private String compoundId;
        private Structure structure;
        private String molecularWeight;
        private String weight;
        private String weightUnit;
        private String moles;
        private String molesUnit;
        private String volume;
        private String volumeUnit;
        private String eq;
        private String chemicalName;
        private String rxnRole;
        private String stoicPurity;
        private String molarity;
        private String molarityUnit;
        private String hazardComments;
        private String saltCode;
        private String saltEq;
        private String comments;

        public StoichiometryRow(String fullNbkBatch, String compoundId, Structure structure, String molecularWeight, String weight,
                                String weightUnit, String moles, String molesUnit, String volume, String volumeUnit, String eq, String chemicalName, String rxnRole, String stoicPurity, String molarity, String molarityUnit, String hazardComments, String saltCode, String saltEq, String comments) {
            this.fullNbkBatch = fullNbkBatch;
            this.compoundId = compoundId;
            this.structure = structure;
            this.molecularWeight = molecularWeight;
            this.weight = weight;
            this.weightUnit = weightUnit;
            this.moles = moles;
            this.molesUnit = molesUnit;
            this.volume = volume;
            this.volumeUnit = volumeUnit;
            this.eq = eq;
            this.chemicalName = chemicalName;
            this.rxnRole = rxnRole;
            this.stoicPurity = stoicPurity;
            this.molarity = molarity;
            this.molarityUnit = molarityUnit;
            this.hazardComments = hazardComments;
            this.saltCode = saltCode;
            this.saltEq = saltEq;
            this.comments = comments;
        }

        public String getMolecularWeight() {
            return molecularWeight;
        }

        public String getWeight() {
            return weight;
        }

        public String getMoles() {
            return moles;
        }

        public String getVolume() {
            return volume;
        }

        public String getEq() {
            return eq;
        }

        public String getFullNbkBatch() {
            return fullNbkBatch;
        }

        public String getCompoundId() {
            return compoundId;
        }

        public Structure getStructure() {
            return structure;
        }

        public String getWeightUnit() {
            return weightUnit;
        }

        public String getMolesUnit() {
            return molesUnit;
        }

        public String getVolumeUnit() {
            return volumeUnit;
        }

        public String getChemicalName() {
            return chemicalName;
        }

        public String getRxnRole() {
            return rxnRole;
        }

        public String getStoicPurity() {
            return stoicPurity;
        }

        public String getMolarity() {
            return molarity;
        }

        public String getMolarityUnit() {
            return molarityUnit;
        }

        public String getHazardComments() {
            return hazardComments;
        }

        public String getSaltCode() {
            return saltCode;
        }

        public String getSaltEq() {
            return saltEq;
        }

        public String getComments() {
            return comments;
        }
    }

    public static class Structure {
        private PdfImage image;

        public Structure(PdfImage image) {
            this.image = image;
        }

        public PdfImage getImage() {
            return image;
        }
    }
}
