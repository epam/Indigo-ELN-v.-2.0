package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;

import java.util.List;

public class RegistrationSummaryModel implements SectionModel {
    private List<RegistrationSummaryRow> rows;

    public RegistrationSummaryModel(List<RegistrationSummaryRow> rows) {
        this.rows = rows;
    }

    public List<RegistrationSummaryRow> getRows() {
        return rows;
    }

    public static class RegistrationSummaryRow {
        private String fullNbkBatch;
        private String totalAmountMade;
        private String registrationStatus;
        private String conversationalBatch;

        public RegistrationSummaryRow(String fullNbkBatch, String totalAmountMade, String registrationStatus, String conversationalBatch) {
            this.fullNbkBatch = fullNbkBatch;
            this.totalAmountMade = totalAmountMade;
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
    }
}
