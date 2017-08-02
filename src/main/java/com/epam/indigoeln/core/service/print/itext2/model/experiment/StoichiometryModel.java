package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

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

        public StoichiometryRow setFullNbkBatch(String fullNbkBatch) {
            this.fullNbkBatch = fullNbkBatch;
            return this;
        }

        public StoichiometryRow setCompoundId(String compoundId) {
            this.compoundId = compoundId;
            return this;
        }

        public StoichiometryRow setStructure(Structure structure) {
            this.structure = structure;
            return this;
        }

        public StoichiometryRow setMolecularWeight(String molecularWeight) {
            this.molecularWeight = molecularWeight;
            return this;
        }

        public StoichiometryRow setWeight(String weight) {
            this.weight = weight;
            return this;
        }

        public StoichiometryRow setWeightUnit(String weightUnit) {
            this.weightUnit = weightUnit;
            return this;
        }

        public StoichiometryRow setMoles(String moles) {
            this.moles = moles;
            return this;
        }

        public StoichiometryRow setMolesUnit(String molesUnit) {
            this.molesUnit = molesUnit;
            return this;
        }

        public StoichiometryRow setVolume(String volume) {
            this.volume = volume;
            return this;
        }

        public StoichiometryRow setVolumeUnit(String volumeUnit) {
            this.volumeUnit = volumeUnit;
            return this;
        }

        public StoichiometryRow setEq(String eq) {
            this.eq = eq;
            return this;
        }

        public StoichiometryRow setChemicalName(String chemicalName) {
            this.chemicalName = chemicalName;
            return this;
        }

        public StoichiometryRow setRxnRole(String rxnRole) {
            this.rxnRole = rxnRole;
            return this;
        }

        public StoichiometryRow setStoicPurity(String stoicPurity) {
            this.stoicPurity = stoicPurity;
            return this;
        }

        public StoichiometryRow setMolarity(String molarity) {
            this.molarity = molarity;
            return this;
        }

        public StoichiometryRow setMolarityUnit(String molarityUnit) {
            this.molarityUnit = molarityUnit;
            return this;
        }

        public StoichiometryRow setHazardComments(String hazardComments) {
            this.hazardComments = hazardComments;
            return this;
        }

        public StoichiometryRow setSaltCode(String saltCode) {
            this.saltCode = saltCode;
            return this;
        }

        public StoichiometryRow setSaltEq(String saltEq) {
            this.saltEq = saltEq;
            return this;
        }

        public StoichiometryRow setComments(String comments) {
            this.comments = comments;
            return this;
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
