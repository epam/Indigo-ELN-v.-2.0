package com.epam.indigoeln.web.dto;

import com.epam.indigoeln.core.model.Experiment;

import java.util.List;

public class ExperimentTablesDTO {

    List<Experiment> openAndCompletedExp;
    List<Experiment> waitingSignatureExp;
    List<Experiment> submittedAndSigningExp;

    public List<Experiment> getOpenAndCompletedExp() {
        return openAndCompletedExp;
    }

    public void setOpenAndCompletedExp(List<Experiment> openAndCompletedExp) {
        this.openAndCompletedExp = openAndCompletedExp;
    }

    public List<Experiment> getWaitingSignatureExp() {
        return waitingSignatureExp;
    }

    public void setWaitingSignatureExp(List<Experiment> waitingSignatureExp) {
        this.waitingSignatureExp = waitingSignatureExp;
    }

    public List<Experiment> getSubmittedAndSigningExp() {
        return submittedAndSigningExp;
    }

    public void setSubmittedAndSigningExp(List<Experiment> submittedAndSigningExp) {
        this.submittedAndSigningExp = submittedAndSigningExp;
    }
}
