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
        private ReagentInfo reagentInfo;
        private String molecularWeight;
        private String weight;
        private String moles;
        private String volume;
        private String eq;
        private String otherInformation;

        public StoichiometryRow(ReagentInfo reagentInfo, String molecularWeight, String weightMg,
                                String molesMm, String volume, String eq, String otherInformation) {
            this.reagentInfo = reagentInfo;
            this.molecularWeight = molecularWeight;
            this.weight = weightMg;
            this.moles = molesMm;
            this.volume = volume;
            this.eq = eq;
            this.otherInformation = otherInformation;
        }

        public ReagentInfo getReagentInfo() {
            return reagentInfo;
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

        public String getOtherInformation() {
            return otherInformation;
        }
    }

    public static class ReagentInfo {
        private PdfImage image;
        private String fullNbkBatch;
        private String compoundId;

        public ReagentInfo(PdfImage molecularImage, String fullNbkBatch, String compoundId) {
            this.image = molecularImage;
            this.fullNbkBatch = fullNbkBatch;
            this.compoundId = compoundId;
        }

        public PdfImage getImage() {
            return image;
        }

        public String getFullNbkBatch() {
            return fullNbkBatch;
        }

        public String getCompoundId() {
            return compoundId;
        }
    }

}
