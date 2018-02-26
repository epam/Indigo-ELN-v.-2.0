/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.PdfImage;

import java.util.List;

/**
 * Implementation of SectionModel interface for preferred compounds.
 */
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
        private PdfImage image;
        private String virtualCompoundId;
        private String name;
        private String description;

        public Structure(PdfImage image, String virtualCompoundId, String name, String description) {
            this.image = image;
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

        public PdfImage getImage() {
            return image;
        }
    }
}
