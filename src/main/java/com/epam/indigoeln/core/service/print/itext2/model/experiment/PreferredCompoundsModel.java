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

        private Structure structure;
        private String notebookBatchNumber;
        private String molWeight;
        private String molFormula;
        private String structureComments;

        public PreferredCompoundsRow(Structure structure,
                                     String notebookBatchNumber, String molWeight, String molFormula,
                                     String structureComments) {
            this.structure = structure;
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



        public Structure getStructure() {
            return structure;
        }

    }

    public static class Structure {
        private String virtualCompoundId;
        private String name;
        private String description;

        public Structure(String virtualCompoundId, String name, String description) {
            this.virtualCompoundId = virtualCompoundId;
            this.name = name;
            this.description = description;
        }

        public String getVirtualCompoundId() {
            return virtualCompoundId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
