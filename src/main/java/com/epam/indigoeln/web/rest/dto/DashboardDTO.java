package com.epam.indigoeln.web.rest.dto;

import java.util.Collection;

public class DashboardDTO {

    Collection<DashboardExperimentDTO> openAndCompletedExp;
    Collection<DashboardExperimentDTO> waitingSignatureExp;
    Collection<DashboardExperimentDTO> submittedAndSigningExp;

    public Collection<DashboardExperimentDTO> getOpenAndCompletedExp() {
        return openAndCompletedExp;
    }

    public void setOpenAndCompletedExp(Collection<DashboardExperimentDTO> openAndCompletedExp) {
        this.openAndCompletedExp = openAndCompletedExp;
    }

    public Collection<DashboardExperimentDTO> getWaitingSignatureExp() {
        return waitingSignatureExp;
    }

    public void setWaitingSignatureExp(Collection<DashboardExperimentDTO> waitingSignatureExp) {
        this.waitingSignatureExp = waitingSignatureExp;
    }

    public Collection<DashboardExperimentDTO> getSubmittedAndSigningExp() {
        return submittedAndSigningExp;
    }

    public void setSubmittedAndSigningExp(Collection<DashboardExperimentDTO> submittedAndSigningExp) {
        this.submittedAndSigningExp = submittedAndSigningExp;
    }
}
