package com.epam.indigoeln.core.chemistry.domain;

import com.epam.indigoeln.core.chemistry.experiment.datamodel.batch.BatchType;

import java.util.List;

public interface StoicModelInterface {

    AmountModel getStoicMoleAmount();

    //Following 2 methods will help to display mMOLES field in stoic
    void setStoicMoleAmount(AmountModel molAmt);

    AmountModel getStoicRxnEquivsAmount();

    //Following 2 methods will help to display EQ field in stoic
    void setStoicRxnEquivsAmount(AmountModel rxnEquivAmt);

    boolean isStoicLimiting();

    //Following 2 methods will help to display LIMITING field in stoic
    void setStoicLimiting(boolean isLimiting);

    String getStoicReactionRole();

    AmountModel getStoicMolarAmount();

    //Following 2 methods will help to display MOLARITY field in stoic
    void setStoicMolarAmount(AmountModel molarAmt);

    //For MolecularWeight
    AmountModel getStoicMolecularWeightAmount();


    //For Weight
    AmountModel getStoicWeightAmount();

    //For Density
    AmountModel getStoicDensityAmount();

    //For Volume
    AmountModel getStoicVolumeAmount();

    void setStoicVolumeAmount(AmountModel volume);

    //For TotalWeight
    AmountModel getTotalWeight();

    //For TotalVolume
    AmountModel getTotalVolume();

    int getStoicTransactionOrder();

    void setStoicTransactionOrder(int transactionOrder);

    AmountModel getStoicPurityAmount();

    boolean isAutoCalcOn();

    boolean shouldApplySigFigRules();

    boolean shouldApplyDefaultSigFigs();

    void applyLatestSigDigits(int defaultSigs);

    AmountModel getPreviousMolarAmount();

    void setPreviousMolarAmount(AmountModel molarAmount);

    void applySigFigRules(AmountModel amt, List<AmountModel> amts);

    BatchType getBatchType();

    void recalcAmounts();

    //ProductBatch specific methods for Stoich
    AmountModel getTheoreticalWeightAmount();

    AmountModel getTheoreticalMoleAmount();

    void setTheoreticalMoleAmount(AmountModel theoreticalMoleAmount);

    void resetAmount(String amountName);

}
