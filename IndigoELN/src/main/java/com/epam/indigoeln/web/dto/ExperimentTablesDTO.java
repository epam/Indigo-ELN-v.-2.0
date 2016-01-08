package com.epam.indigoeln.web.dto;

import com.epam.indigoeln.core.model.Experiment;

import java.util.Collection;

public class ExperimentTablesDTO {

    Collection<Experiment> openAndCompletedExp;
    Collection<Experiment> waitingSignatureExp;
    Collection<Experiment> submittedAndSigningExp;

    public Collection<Experiment> getOpenAndCompletedExp() {
        return openAndCompletedExp;
    }

    public void setOpenAndCompletedExp(Collection<Experiment> openAndCompletedExp) {
        this.openAndCompletedExp = openAndCompletedExp;
    }

    public Collection<Experiment> getWaitingSignatureExp() {
        return waitingSignatureExp;
    }

    public void setWaitingSignatureExp(Collection<Experiment> waitingSignatureExp) {
        this.waitingSignatureExp = waitingSignatureExp;
    }

    public Collection<Experiment> getSubmittedAndSigningExp() {
        return submittedAndSigningExp;
    }

    public void setSubmittedAndSigningExp(Collection<Experiment> submittedAndSigningExp) {
        this.submittedAndSigningExp = submittedAndSigningExp;
    }
}
