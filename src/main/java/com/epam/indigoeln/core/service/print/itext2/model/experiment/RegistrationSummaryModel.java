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

import java.util.List;

/**
 * Implementation of SectionModel interface for registration summary.
 */
public class RegistrationSummaryModel implements SectionModel {
    private List<RegistrationSummaryRow> rows;

    public RegistrationSummaryModel(List<RegistrationSummaryRow> rows) {
        this.rows = rows;
    }

    public List<RegistrationSummaryRow> getRows() {
        return rows;
    }

    /**
     * Inner class which describes summary row.
     */
    public static class RegistrationSummaryRow {
        private String fullNbkBatch;
        private String totalAmountMade;
        private String totalAmountMadeUnit;
        private String registrationStatus;
        private String conversationalBatch;


        public RegistrationSummaryRow(String fullNbkBatch, String totalAmountMade,
                                      String totalAmountMadeUnit, String registrationStatus,
                                      String conversationalBatch) {
            this.fullNbkBatch = fullNbkBatch;
            this.totalAmountMade = totalAmountMade;
            this.totalAmountMadeUnit = totalAmountMadeUnit;
            this.registrationStatus = registrationStatus;
            this.conversationalBatch = conversationalBatch;
        }

        public String getFullNbkBatch() {
            return fullNbkBatch;
        }

        public String getTotalAmountMade() {
            return totalAmountMade;
        }

        public String getRegistrationStatus() {
            return registrationStatus;
        }

        public String getConversationalBatch() {
            return conversationalBatch;
        }

        public String getTotalAmountMadeUnit() {
            return totalAmountMadeUnit;
        }
    }
}
