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
        private String virtualCompoundId;
        private Stereoismoer stereoismoer;
        private String notebookBatchNumber;
        private String molWeight;
        private String molFormula;
        private String structureComments;

        public PreferredCompoundsRow(String virtualCompoundId, Stereoismoer stereoismoer,
                                     String notebookBatchNumber, String molWeight, String molFormula,
                                     String structureComments) {

            this.virtualCompoundId = virtualCompoundId;
            this.stereoismoer = stereoismoer;
            this.notebookBatchNumber = notebookBatchNumber;
            this.molWeight = molWeight;
            this.molFormula = molFormula;
            this.structureComments = structureComments;
        }

        public String getNotebookBatchNumber() {
            return notebookBatchNumber;
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

        public String getVirtualCompoundId() {
            return virtualCompoundId;
        }

        public Stereoismoer getStereoismoer() {
            return stereoismoer;
        }

    }

    public static class Stereoismoer{
        private String name;
        private String description;

        public Stereoismoer(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
