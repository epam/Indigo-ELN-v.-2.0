package com.epam.indigoeln.web.rest.dto;

import java.util.Collection;

public class DashboardDTO {

    Collection<DashboardRowDTO> openAndCompletedExp;
    Collection<DashboardRowDTO> waitingSignatureExp;
    Collection<DashboardRowDTO> submittedAndSigningExp;

    public Collection<DashboardRowDTO> getOpenAndCompletedExp() {
        return openAndCompletedExp;
    }

    public void setOpenAndCompletedExp(Collection<DashboardRowDTO> openAndCompletedExp) {
        this.openAndCompletedExp = openAndCompletedExp;
    }

    public Collection<DashboardRowDTO> getWaitingSignatureExp() {
        return waitingSignatureExp;
    }

    public void setWaitingSignatureExp(Collection<DashboardRowDTO> waitingSignatureExp) {
        this.waitingSignatureExp = waitingSignatureExp;
    }

    public Collection<DashboardRowDTO> getSubmittedAndSigningExp() {
        return submittedAndSigningExp;
    }

    public void setSubmittedAndSigningExp(Collection<DashboardRowDTO> submittedAndSigningExp) {
        this.submittedAndSigningExp = submittedAndSigningExp;
    }
}
