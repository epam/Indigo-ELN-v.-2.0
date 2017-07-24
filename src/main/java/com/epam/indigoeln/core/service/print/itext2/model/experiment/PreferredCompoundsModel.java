package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;

import java.util.List;

public class PreferredCompoundsModel implements SectionModel {
    private List<PreferredCompoundsRow> rows;

    public PreferredCompoundsModel(List<PreferredCompoundsRow> rows) {
        this.rows = rows;
    }

    public List<PreferredCompoundsRow> getRows() {
        return rows;
    }

    public static class PreferredCompoundsRow {
        private String structure;
        private String notebookBatchNUmber;
        private String molWeight;
        private String molFormula;
        private String structureComments;

        public PreferredCompoundsRow(String structure, String notebookBatchNUmber,
                                     String molWeight, String molFormula, String structureComments) {
            this.structure = structure;
            this.notebookBatchNUmber = notebookBatchNUmber;
            this.molWeight = molWeight;
            this.molFormula = molFormula;
            this.structureComments = structureComments;
        }

        public String getStructure() {
            return structure;
        }

        public String getNotebookBatchNmber() {
            return notebookBatchNUmber;
        }

        public String getMolWeight() {
            return molWeight;
        }

        public String getMolFormula() {
            return molFormula;
        }

        public String getStructureComments() {
            return structureComments;
        }
    }
}
