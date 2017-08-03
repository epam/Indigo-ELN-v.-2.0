package com.epam.indigoeln.core.chemistry;

import com.epam.indigoeln.core.chemistry.domain.*;
import com.epam.indigoeln.core.chemistry.experiment.businessmodel.stoichiometry.ComparatorStoicAdditionOrder;
import com.epam.indigoeln.core.chemistry.experiment.common.units.UnitType;
import com.epam.indigoeln.core.chemistry.experiment.datamodel.batch.BatchType;
import com.epam.indigoeln.core.chemistry.experiment.utils.CeNNumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.indigoeln.core.util.EqualsUtil.doubleEqZero;
import static com.epam.indigoeln.core.util.EqualsUtil.doubleNumEq;

//this class plays the role of StoichiometeryModel in 1.1
//Also look at ReactionStepModelUtils.java
public class StoichCalculator {

    private static final Log LOGGER = LogFactory.getLog(StoichCalculator.class);
    private final ReactionStepModel rxnStepModel;

//    private String pageType; // Parallel/Med_chem etc

    public StoichCalculator(ReactionStepModel mrxnStepModel) {
        this.rxnStepModel = mrxnStepModel;
    }

    /**
     * Use to have the ReactionStep's Stoichiometry reevaluated Resets limiting reagent if one can't be found.
     */
    public void recalculateStoich() {
        LOGGER.debug("StoichCalculator.recalculateStoich().enter");
        StoicModelInterface rb;
        // Determine the limiting reagent
        rb = setLimitingReagent();
        // Make sure there is at least one Intended Product if there is a limiting Reagent.
        if (rb != null) {
            // Now make sure everyone is up to date
            recalculateMolesBasedOnLimitingReagent(rb);
            recalculateRxnEquivsBasedOnLimitingReagentMoles(rb);
        }
        recalculateProductsBasedOnStoich();
        LOGGER.debug("StoichCalculator.recalculateStoich().exit");
    }

    public void recalculateStoichBasedOnBatch(StoicModelInterface rb, boolean calcMolesOnly) {
        LOGGER.debug("StoichCalculator.recalculateStoichBasedOnBatch().enter");
        if (rb != null) {
            // see if this batch(List) is qualified as LimitReag
            if (rb.isStoicLimiting() && !canBeLimiting(rb)) {
                rb.setStoicLimiting(false);
            }
            // find limiting reagent
            StoicModelInterface limitingReag = findLimitingReagent();
            if (limitingReag == null) {
                limitingReag = setLimitingReagent();
            }
            if (rb.getStoicReactionRole().equals(BatchType.SOLVENT.toString())) {
                recalculateSolventAmounts(rb, limitingReag);
                // to recalculate Molarity or Volume
            }
            if (limitingReag != null) {// vb 5/8 limitingBatch is null if the user hasn't entered stoic info and is in
                if (CeNNumberUtils.doubleEquals(limitingReag.getStoicRxnEquivsAmount().getValueInStdUnitsAsDouble(), 0.0,
                        0.0001)) {
                    resetRxnEquivs(limitingReag); // Puts back to 1.0
                }
                // Catalyst to get calcs going in case limiting reag isn't filled in.
                // Make sure the limiting reagent is set and has a value.
                if (limitingReag.getStoicMoleAmount().isValueDefault()) {
                    recalculateAmountsForBatch(rb, limitingReag, calcMolesOnly);
                }

                // Update Molarity for all solvents, if moles of limiting reagent is modified
                recalculateMolarAmountForSolvent(limitingReag.getStoicMoleAmount());
                findAndRecalculateVolumeForSolvents(rxnStepModel.getStoicElementListInTransactionOrder(), limitingReag.getStoicMoleAmount(), null);
            }

            // unset any other limiting reagent flag
            if (rb.isStoicLimiting()) {
                clearLimitingReagentFlags(rb);
            }
            // re calculate the moles of all the other batches in stoic based on limit reag moles
            recalculateMolesBasedOnLimitingReagent(rb);

            recalculateProductsBasedOnStoich();

        }
        LOGGER.debug("StoichCalculator.recalculateStoichBasedOnBatch().exit");
    }

    private void recalculateProductsBasedOnStoich() {
        LOGGER.debug("StoichCalculator.recalculateProductsBasedOnStoich().enter");
        StoicModelInterface limitingReag = findLimitingReagent();

        if (limitingReag != null) {
            // addProductIfNeeded(); // For MED-CHEM
            // used to keep a place marker for stoich products if there isn't one
            // allows calcs to be setup such that the user can see the effect of
            // their changes.
            for (StoicModelInterface pb : getProductBatches()) {
                // updateIntendedProductPrecursors(); // For MED_CHEM
                recalculateProductAmounts(limitingReag, pb);
            }
        }
        LOGGER.debug("StoichCalculator.recalculateProductsBasedOnStoich().exit");
    }

    /**
     * If only Volume is set for the batch then don't display reaction eq value
     */
    private boolean isReactionEquivMeaningless(StoicModelInterface rb) {
        // if only Volume is set for it then don't display eq value

        Supplier<Boolean> cond = () -> !rb.getStoicVolumeAmount().isCalculated() && rb.getStoicMoleAmount().isCalculated();

        return  cond.get() &&
                rb.getStoicRxnEquivsAmount().isCalculated() && rb.getStoicDensityAmount().isCalculated() &&
                rb.getStoicMolarAmount().isCalculated();
    }

    private boolean canBeLimiting(StoicModelInterface rb) {
        return !(rb.getStoicReactionRole().equals(BatchType.SOLVENT.toString()) || isReactionEquivMeaningless(rb));
    }

    // Find currently already marked limiting reagent in the list.
    private StoicModelInterface findLimitingReagent() {
        StoicModelInterface result = null;

        if (rxnStepModel != null) {
            for (StoicModelInterface reagent : rxnStepModel.getStoicElementListInTransactionOrder()) {
                if (reagent.isStoicLimiting() && canBeLimiting(reagent)) {
                    result = reagent;
                    break;
                }
            }
        }

        return result;
    }

    // Find current limiting reagent.Identify new limiting reagent if there is none currently.
    // lowest mole value batch(List) is selected as Limiting Reagent and set equivs to 1.0 if current value is zero
    private StoicModelInterface setLimitingReagent() {
        StoicModelInterface result;
        result = findLimitingReagent();
        if (result == null) {
            for (StoicModelInterface test : getReagentBatches()) {
                test.setStoicLimiting(false);
                if (canBeLimiting(test) && (result == null ||
                        test.getStoicMoleAmount().doubleValue() < result.getStoicMoleAmount().doubleValue())) {
                    result = test;
                }
            }
            if (result != null) {
                result.setStoicLimiting(true);
                if (result.getStoicRxnEquivsAmount().isValueDefault())
                    resetRxnEquivs(result);
            }
        }
        return result;
    }

    private void recalculateSolventAmounts(StoicModelInterface rb, StoicModelInterface limitingReagent) {
        LOGGER.debug("StoichCalculator.recalculateSolventAmounts().enter");
        if (limitingReagent == null)
            return;
        AmountModel molarAmount = rb.getStoicMolarAmount();
        AmountModel volumeAmount = rb.getStoicVolumeAmount();
        ArrayList<AmountModel> volumeAmountList = getVolumeAmountsForAllSolvents();
        if (doubleEqZero(molarAmount.getValueInStdUnitsAsDouble()) && doubleEqZero(volumeAmount.getValueInStdUnitsAsDouble()))
            return;

        List<StoicModelInterface> reagents = getReagentBatches();
        for (int i = 0; i < reagents.size(); i++) {
            StoicModelInterface b = reagents.get(i);
            // if solvant go ahead
            if (b.getStoicReactionRole().equals(BatchType.SOLVENT.toString()) && b.equals(rb)) {
                // if this batch is the editing batch
                if (volumeAmount.isCalculated()) {
                    // Case for user entry for Molar amount
                    if (!molarAmount.isCalculated() && !doubleEqZero(molarAmount.getValueInStdUnitsAsDouble())) {
                        recalculateVolumeForSolvent(limitingReagent.getStoicMoleAmount(), rb, volumeAmountList);
                        molarAmount = rb.getStoicMolarAmount();
                        updateMolarAmountsForSolvent(molarAmount.getValueInStdUnitsAsDouble(), molarAmount.getSigDigits());
                        molarAmount.setCalculated(false);
                        rb.setPreviousMolarAmount(molarAmount);
                    }
                } else if (molarAmount.isCalculated()) {
                    if (doubleEqZero(molarAmount.getValueInStdUnitsAsDouble()) || areAllVolumesUserEnteredForSovents())
                        recalculateMolarAmountForSolvent(limitingReagent.getStoicMoleAmount());
                    // User entered Volume amount
                    // once Molar amounts are updated for the volume entered, it is required
                    // to find and update the volume that was calculated.
                    findAndRecalculateVolumeForSolvents(reagents, limitingReagent.getStoicMoleAmount(), volumeAmount);
                    rb.getStoicVolumeAmount().setCalculated(false);
                } else {
                    // Volume and Molarity are both user entered, check if Molar Amount
                    // is different than other solvents, if different then user have entered
                    // for Molarity
                    AmountModel tempMolarAmt = rb.getPreviousMolarAmount();
                    if (!doubleNumEq(tempMolarAmt.getValueInStdUnitsAsDouble(), molarAmount.getValueInStdUnitsAsDouble())) {
                        // Molarity entered by the user
                        updateMolarAmountsForSolvent(rb.getStoicMolarAmount().getValueInStdUnitsAsDouble(), rb
                                .getStoicMolarAmount().getSigDigits());
                        applySigDigitsToAllSolventMolarites(rb.getStoicMolarAmount());
                        rb.getStoicMolarAmount().setCalculated(false);
                        // Calculate Volume for this solvent
                        recalculateVolumeForSolvent(limitingReagent.getStoicMoleAmount(), rb, volumeAmountList);
                        rb.getStoicVolumeAmount().setCalculated(true);
                        rb.setPreviousMolarAmount(rb.getStoicMolarAmount());
                    } else {
                        if (areAllVolumesUserEnteredForSovents()) {
                            // recalculate Molarity with the updated total volume
                            recalculateMolarAmountForSolvent(limitingReagent.getStoicMoleAmount());
                            rb.getStoicVolumeAmount().setCalculated(false);
                            rb.getStoicMolarAmount().setCalculated(true);
                        } else {
                            // once Molar amounts are updated for the volume entered, it is required
                            // to find and update the volume that was calculated.
                            findAndRecalculateVolumeForSolvents(reagents, limitingReagent.getStoicMoleAmount(), volumeAmount);
                        }
                    }
                }
            }
        }
        LOGGER.debug("StoichCalculator.recalculateSolventAmounts().exit");
    }

    private void recalculateAmountsForBatch(StoicModelInterface limtReag, StoicModelInterface targetReag, boolean calcMolesOnly) {
        LOGGER.debug("StoichCalculator.recalculateAmountsForBatch().enter");
        if (limtReag != null && targetReag != null && limtReag != targetReag && targetReag.isAutoCalcOn()) {
            // Need to decide if we are updating RxnEquivs or a weight amount
            if (!targetReag.getStoicRxnEquivsAmount().isCalculated() ||
                    (targetReag.getStoicMoleAmount().isCalculated() &&
                            targetReag.getStoicWeightAmount().isCalculated() &&
                            targetReag.getStoicVolumeAmount().isCalculated())) {
                recalculateMoleAmountForBatch(limtReag, targetReag);
            }

            if (!calcMolesOnly && limtReag.getStoicMoleAmount().doubleValue() > 0.0 && targetReag.getStoicRxnEquivsAmount().isCalculated()) {
                // don'e bother if sourceReag's mole amount isn't > 0
                // Update the rxnEquivs for the batch
                recalculateRxnEquivsForBatch(limtReag, targetReag);
            }
        }
        LOGGER.debug("StoichCalculator.recalculateAmountsForBatch().exit");
    }

    private void resetRxnEquivs(StoicModelInterface ab) {
        if (doubleEqZero(ab.getStoicRxnEquivsAmount().doubleValue()) && ab.getStoicRxnEquivsAmount().isCalculated()) {
            AmountModel rxnEquiv = ab.getStoicRxnEquivsAmount();
            rxnEquiv.setSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
            rxnEquiv.setValue("1.00", true);
            // ab can be Monomer list or a simple batch. So call the setter so that all the models in list will targeted
            ab.setStoicRxnEquivsAmount(rxnEquiv);
        }
    }

    /**
     * Removes isLimiting() flag from any reagent in the stoich model. set rb to null if all reagents are to have their limiting
     * flag unset. Otherwise pass in the limiting reagent after setting its flag and all other stoich batches will have their flags
     * unset.
     */
    private void clearLimitingReagentFlags(StoicModelInterface exceptForThisReagent) {
        rxnStepModel.getStoicElementListInTransactionOrder().stream().filter(b -> !b.equals(exceptForThisReagent)).forEach(b -> b.setStoicLimiting(false));
    }

    /**
     * Recalculates Moles for all batches in this ReactionStep given the limiting Reagent. Use when limiting reagent is set, or the
     * amount is changed in some way.
     */
    private void recalculateMolesBasedOnLimitingReagent(StoicModelInterface limitingAB) {
        LOGGER.debug("StoichCalculator.recalculateMolesBasedOnLimitingReagent().enter");
        if (limitingAB != null && canBeLimiting(limitingAB)) {
            // So in effect you are saying that if the volume of the limiting reagent is zero you don't
// want to permit any reagent from being updated by values in the limiting reagent.
// That is an incorrect assumption as limiting reagent can be dry or neat.
// I believe this check-in will break our calculations when volume of limiting reagent is zero.
// Try again.
//nothing
// Need to not calculate Amounts for SOLVENTS
// if the batch 'limitingAB' is a limited reagent then all the batches should be recalculated.
// if the batch 'limitingAB' is NOT a limited reagent then only this batch should be recalculated basing on the limited one
            getReagentBatches().stream().filter(b -> !b.equals(limitingAB) && b.isAutoCalcOn()).forEach(b -> {
                Supplier<Boolean> cond = () -> b.getStoicMoleAmount().doubleValue() > 0.0 ||
                        CeNNumberUtils.doubleEquals(b.getStoicMoleAmount().doubleValue(), 0.0) &&
                                b.getStoicVolumeAmount().doubleValue() > 0.0;

                if (CeNNumberUtils.doubleEquals(limitingAB.getStoicVolumeAmount().doubleValue(), 0.0) && CeNNumberUtils.doubleEquals(limitingAB.getStoicWeightAmount().doubleValue(), 0.0)) {
                    if (b.isStoicLimiting()) {
                        AmountModel am = b.getStoicMoleAmount();
                        AmountModel tmpAmt = new AmountModel(am.getUnitType());
                        tmpAmt.deepCopy(am);
                        limitingAB.setStoicMoleAmount(tmpAmt);
                    }
                } else if (b.getStoicRxnEquivsAmount().getValueInStdUnitsAsDouble() > 0.0 && cond.get() || limitingAB.isStoicLimiting()) {
                    if (!b.getStoicReactionRole().equals(BatchType.SOLVENT.toString())) {
                        // if the batch 'limitingAB' is a limited reagent then all the batches should be recalculated.
                        if (limitingAB.isStoicLimiting()) {
                            recalculateAmountsForBatch(limitingAB, b, false);
                            // if the batch 'limitingAB' is NOT a limited reagent then only this batch should be recalculated basing on the limited one
                        } else if (b.isStoicLimiting()) {
                            recalculateAmountsForBatch(b, limitingAB, false);
                        }
                    }
                } else if (b.getStoicMoleAmount().doubleValue() > 0.0) {
                    b.resetAmount(CeNConstants.MOLE_AMOUNT);
                }
            });
            updateActualProductTheoAmounts(limitingAB);
        }
        LOGGER.debug("StoichCalculator.recalculateMolesBasedOnLimitingReagent().exit");
    }

    private void recalculateMolarAmountForSolvent(AmountModel limitingMole) {
        LOGGER.debug("StoichCalculator.recalculateMolarAmountForSolvent().enter");
        ArrayList<AmountModel> amts = new ArrayList<>();
        ArrayList<AmountModel> amountsList = getVolumeAmountsForAllSolvents();
        AmountModel tempAmount;
        double totalVolume = 0;
        // adding up the total volume
        for (AmountModel anAmountsList : amountsList) {
            tempAmount = anAmountsList;
            totalVolume += tempAmount.getValueInStdUnitsAsDouble();
        }
        amts.addAll(amountsList);
        amts.add(limitingMole);
        // Molarity calculation
        double molarity = limitingMole.getValueInStdUnitsAsDouble() / totalVolume;
        updateMolarAmountsForSolvent(molarity, CeNNumberUtils.getSmallestSigFigsFromAmountModelList(amts));
        amts.clear();
        LOGGER.debug("StoichCalculator.recalculateMolarAmountForSolvent().exit");
    }

    private void findAndRecalculateVolumeForSolvents(List<StoicModelInterface> reagents, AmountModel limitingMoleAmount, AmountModel currentVolumeAmount) {

        // update the calculated volume to current value.
        for (StoicModelInterface batch : reagents) {
            if (batch.getStoicReactionRole().equals(BatchType.SOLVENT.toString())) {
                AmountModel tempVolumeAmt = batch.getStoicVolumeAmount();
                if (!tempVolumeAmt.equals(currentVolumeAmount) && tempVolumeAmt.isCalculated()) {
                    recalculateVolumeForSolvent(limitingMoleAmount, batch, getVolumeAmountsForAllSolvents());
                }
            }
        }
    }

    private void updateActualProductTheoAmounts(StoicModelInterface limitingReag) {
        LOGGER.debug("StoichCalculator.updateActualProductTheoAmounts().enter");
        if (limitingReag != null && canBeLimiting(limitingReag)) {
            for (StoicModelInterface pb : getActualProductBatches()) {
                // Because the amount might indicate user edits we need to make sure the new version
                // doesn't. When set..Amount() is called the modified flag can cause other events to
                // trigger before we are ready. Make sure the calc'd flag is set properly first.
                AmountModel tmpAmt = new AmountModel(UnitType.MOLES);
                tmpAmt.deepCopy(limitingReag.getStoicMoleAmount());
                tmpAmt.setCalculated(true);
                pb.setTheoreticalMoleAmount(tmpAmt);
                pb.recalcAmounts(); // Updates the theo weight and yield
            }
        }
        LOGGER.debug("StoichCalculator.updateActualProductTheoAmounts().exit");
    }

    private void recalculateProductAmounts(StoicModelInterface limitingReag, StoicModelInterface prodBatch) {
        LOGGER.debug("StoichCalculator.recalculateProductAmounts().enter");
        if (limitingReag != null && prodBatch.isAutoCalcOn()) {
            // if moleAmount are not = limiting moleAmount and not set by the user, set it.
            // send to recalc based on moleAmount. Need to be sure that weightAmount isn't
            // edited by the user either.
            if (!prodBatch.getStoicRxnEquivsAmount().isCalculated() ||
                    (prodBatch.getStoicWeightAmount().isCalculated() && prodBatch.getStoicMoleAmount().isCalculated()
                            && !limitingReag.getStoicMoleAmount().equals(prodBatch.getStoicMoleAmount()))) {
                // Need to set calc flag regardless of limitingReag state.
                AmountModel amtTemp = (AmountModel) limitingReag.getStoicMoleAmount().deepClone();
                double targetMoles = amtTemp.doubleValue() * prodBatch.getStoicRxnEquivsAmount().getValueInStdUnitsAsDouble();
                amtTemp.setValue(targetMoles);
                amtTemp.setCalculated(true);
                // Causes batch to recalculate amounts based on Mole change.
                prodBatch.setStoicMoleAmount(amtTemp);
                if (limitingReag.shouldApplySigFigRules()) {
                    prodBatch.applyLatestSigDigits(amtTemp.getSigDigits());
                } else {
                    prodBatch.applyLatestSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
                }
            }
            updateActualProductTheoAmounts(limitingReag);
        }
        LOGGER.debug("StoichCalculator.recalculateProductAmounts().exit");
    }

    private ArrayList<AmountModel> getVolumeAmountsForAllSolvents() {
        List<StoicModelInterface> reagents = getReagentBatches();
        ArrayList<AmountModel> list = new ArrayList<>(reagents.size());
        // if solvent go ahead
        list.addAll(reagents.stream().filter(b -> b.getStoicReactionRole().equals(BatchType.SOLVENT.toString())).map(StoicModelInterface::getStoicVolumeAmount).collect(Collectors.toList()));
        return list;
    }// end of method

    private void updateMolarAmountsForSolvent(double value, int sigDigits) {
        List<StoicModelInterface> reagents = getReagentBatches();
        // Loop for reagents
        // if solvent then set the Molar amount
        reagents.stream().filter(b -> b.getStoicReactionRole().equals(BatchType.SOLVENT.toString())).forEach(b -> {
            AmountModel actualMolarModel = b.getStoicMolarAmount();
            if (actualMolarModel.isCalculated()) {
                AmountModel molarModel = (AmountModel) actualMolarModel.deepClone();
                molarModel.setValueInStdUnits(value);
                molarModel.setSigDigits(sigDigits);
                b.setStoicMolarAmount(molarModel);
                b.setPreviousMolarAmount(b.getStoicMolarAmount());
            }
        });
    }

    private boolean areAllVolumesUserEnteredForSovents() {
        List<StoicModelInterface> reagents = getReagentBatches();
        for (StoicModelInterface b : reagents) {
            // if solvent go ahead
            if (b.getStoicReactionRole().equals(BatchType.SOLVENT.toString()) && b.getStoicVolumeAmount().isCalculated()) {
                return false;
            }
        }
        return true;
    }

    private void recalculateVolumeForSolvent(AmountModel limitingMole, StoicModelInterface rb, ArrayList<AmountModel> volumeAmounts) {
        ArrayList<AmountModel> amts = new ArrayList<>();
        AmountModel tempVolumeAmt;
        double tempMolarAmtValue = rb.getStoicMolarAmount().getValueInStdUnitsAsDouble();
        double calcVolume;
        amts.add(limitingMole);
        amts.add(rb.getStoicMolarAmount());

        if (tempMolarAmtValue > 0) {
            calcVolume = limitingMole.getValueInStdUnitsAsDouble() / tempMolarAmtValue;
        } else
            return;
        // CalcVolume need to be subtracted with volumes of other solvents
        for (AmountModel volumeAmount : volumeAmounts) {
            tempVolumeAmt = volumeAmount;
            if (!tempVolumeAmt.equals(rb.getStoicVolumeAmount())) {
                calcVolume -= tempVolumeAmt.getValueInStdUnitsAsDouble();
                amts.add(tempVolumeAmt);
                if (!doubleEqZero(tempVolumeAmt.getValueInStdUnitsAsDouble()))
                    tempVolumeAmt.setCalculated(false);
            }
        }
        AmountModel actaulVolumeAmt = rb.getStoicVolumeAmount();
        AmountModel volumeAmt = (AmountModel) actaulVolumeAmt.deepClone();
        volumeAmt.setValueInStdUnits(calcVolume, true);
        volumeAmt.setSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
        rb.setStoicVolumeAmount(volumeAmt);
        amts.clear();
    }

    private void applySigDigitsToAllSolventMolarites(AmountModel molarAmt) {
        ArrayList<AmountModel> amountsList = getMolarAmountsForAllSolvents();
        amountsList.stream().filter(tempAmt -> !tempAmt.equals(molarAmt)).forEach(tempAmt -> tempAmt.setSigDigits(molarAmt.getSigDigits()));
    }

    private ArrayList<AmountModel> getMolarAmountsForAllSolvents() {
        List<StoicModelInterface> reagents = getReagentBatches();
        ArrayList<AmountModel> list = new ArrayList<>(reagents.size());
        // if solvent go ahead
        list.addAll(reagents.stream().filter(b -> b.getStoicReactionRole().equals(BatchType.SOLVENT.toString())).map(StoicModelInterface::getStoicMolarAmount).collect(Collectors.toList()));
        return list;
    }// end of method

    private void recalculateMoleAmountForBatch(StoicModelInterface limtReag, StoicModelInterface targetReag) {
        // The last check here is literal: sourceReag != the same object as targetReag
        if (limtReag != null && targetReag != null && limtReag != targetReag && targetReag.isAutoCalcOn()) {
            // Equiv amounts are always scalar so StdUnits are the same as the value itself
            double limitingRxnEquivs = limtReag.getStoicRxnEquivsAmount().doubleValue();
            // ??? should we get value in std unit or the unit user selected ? molAmt.getValueInStdUnitsAsDouble()
            double limitingMoles = limtReag.getStoicMoleAmount().getValueInStdUnitsAsDouble();
            if (targetReag instanceof BatchesList<?>) {
                BatchesList<MonomerBatchModel> blist = (BatchesList<MonomerBatchModel>) targetReag;
                if (blist.getBatchModels() == null)
                    return;
                for (MonomerBatchModel batchModel : blist.getBatchModels()) {
                    ArrayList<AmountModel> amts = new ArrayList<>();
                    // RxnEquivsAmount is scalar, hence no need for std units.
                    if (CeNNumberUtils.doubleEquals(batchModel.getStoicRxnEquivsAmount().doubleValue(), 0.0, 0.00001)) {
                        // somehow the targetReag's equivs weren't set.
                        // default equivs = 1
                        resetRxnEquivs(batchModel);
                    }
                    // (targetMoles * ab.getRxnEquivs()) = (limitingMoles * limitingRxnEquivs())
                    double targetMoles = limitingMoles * (batchModel.getStoicRxnEquivsAmount().doubleValue() / limitingRxnEquivs);
                    amts.add(limtReag.getStoicMoleAmount());
                    amts.add(batchModel.getStoicRxnEquivsAmount());
                    amts.add(limtReag.getStoicRxnEquivsAmount());

                    // to trigger calc of weight ( recalcAmount() method )
                    AmountModel molModelActual = batchModel.getStoicMoleAmount();
                    AmountModel molModel = (AmountModel) molModelActual.deepClone();

                    molModel.setValueInStdUnits(targetMoles, true);
                    molModel.setUnit(limtReag.getStoicMoleAmount().getUnit());
                    batchModel.setMoleAmount(molModel);
                    batchModel.applySigFigRules(batchModel.getStoicMoleAmount(), amts);
                }
            } else {

                ArrayList<AmountModel> amts = new ArrayList<>();
                // RxnEquivsAmount is scalar, hence no need for std units.
                if (CeNNumberUtils.doubleEquals(targetReag.getStoicRxnEquivsAmount().doubleValue(), 0.0, 0.00001)) {
                    // somehow the targetReag's equivs weren't set.
                    // default equivs = 1
                    resetRxnEquivs(targetReag);
                }
                // (targetMoles * ab.getRxnEquivs()) = (limitingMoles * limitingRxnEquivs())
                double targetMoles = limitingMoles * (targetReag.getStoicRxnEquivsAmount().doubleValue() / limitingRxnEquivs);
                amts.add(limtReag.getStoicMoleAmount());
                amts.add(targetReag.getStoicRxnEquivsAmount());
                amts.add(limtReag.getStoicRxnEquivsAmount());
                AmountModel molModel = (AmountModel) targetReag.getStoicMoleAmount().deepClone();
                // ??Issue is whether to set in std units or in units that of limReag's
                molModel.setValueInStdUnits(targetMoles, true);
                molModel.setUnit(limtReag.getStoicMoleAmount().getUnit());
                targetReag.setStoicMoleAmount(molModel);
                targetReag.applySigFigRules(targetReag.getStoicMoleAmount(), amts);
            }
        }
    }

    private void recalculateRxnEquivsForBatch(StoicModelInterface limtReag, StoicModelInterface targetReagent) {
        if (limtReag != null && targetReagent != null && targetReagent.isAutoCalcOn() && targetReagent.getStoicRxnEquivsAmount().isCalculated()) {
            // Need to account for interesting ratios like a limiting reagent that has
            // a rxnEquiv of 2 while the target only has a 1.
            // Invariant limitingRxnEquivs != 0.0
            double result = 1.0; // default rxnEquiv amount.
            // TODO: when is 1.0 going to fall through and make this calc wrong?

            double sourceMoles = limtReag.getStoicMoleAmount().getValueInStdUnitsAsDouble();
            double sourceRxnEquivs = limtReag.getStoicRxnEquivsAmount().doubleValue();
            if (targetReagent instanceof BatchesList<?>) {
                BatchesList<MonomerBatchModel> blist = (BatchesList<MonomerBatchModel>) targetReagent;
                if (blist.getBatchModels() == null)
                    return;

                for (MonomerBatchModel batchModel : blist.getBatchModels()) {
                    double targetReagMoles = batchModel.getStoicMoleAmount().getValueInStdUnitsAsDouble();

                    ArrayList<AmountModel> amts = new ArrayList<>();
                    if (targetReagMoles > 0.0) {
                        amts.add(batchModel.getStoicMoleAmount());
                    }
                    // Make sure targetReagMoles > 0
                    if (doubleEqZero(targetReagMoles) && batchModel.getStoicMolecularWeightAmount().doubleValue() > 0.0) {
                        targetReagMoles = batchModel.getStoicWeightAmount().getValueInStdUnitsAsDouble() / batchModel.getStoicMolecularWeightAmount().doubleValue();
                        amts.add(batchModel.getStoicMolecularWeightAmount());
                        amts.add(batchModel.getStoicWeightAmount());
                        if (batchModel.getStoicPurityAmount().doubleValue() < 100d && batchModel.getStoicPurityAmount().doubleValue() > 0.0) {
                            result = result * (batchModel.getStoicPurityAmount().doubleValue() / 100);
                            amts.add(batchModel.getStoicPurityAmount());
                        }
                    }
                    if (batchModel.getStoicRxnEquivsAmount().isCalculated() && targetReagMoles > 0.0) {
// need to use version from Singleton in else clause below.  This version creates multiples when limiting rxn equivs < 0.
// (targetReagMoles * targetReag.getRxnEquivs()) = (sourceMoles * sourceReag.getRxnEquivs())
                        // (targetReagMoles / sourceMoles) = (targetReag.getRxnEquivs() / sourceReag.getRxnEquivs())
                        // RxnEquivsAmount is scalar, hence no need for std units.
                        double testResult = (targetReagMoles / sourceMoles) * sourceRxnEquivs;
                        // Doing this is wrong!
                        if (testResult >= 0.0)
                            result = testResult;
                        // RxnEquivs is a scalar Unit, though Mole amounts are currently only mMole amounts.
                        // Hence we can simply set the result, but it is safer tu use InStdUnits() method
                        // as there is noise of adding mole amounts in the future.
                        amts.add(limtReag.getStoicMoleAmount());
                        amts.add(limtReag.getStoicRxnEquivsAmount());
                        batchModel.getStoicRxnEquivsAmount().setValueInStdUnits(result, true);
                        batchModel.applySigFigRules(batchModel.getStoicRxnEquivsAmount(), amts);
                    }
                }//for each batch
            } else {
                MonomerBatchModel batchModel = (MonomerBatchModel) targetReagent;
                double targetReagMoles = batchModel.getStoicMoleAmount().getValueInStdUnitsAsDouble();

                ArrayList<AmountModel> amts = new ArrayList<>();
                if (targetReagMoles > 0.0) {
                    amts.add(batchModel.getStoicMoleAmount());
                }
                // Make sure targetReagMoles > 0
                if (doubleEqZero(targetReagMoles) && batchModel.getStoicMolecularWeightAmount().doubleValue() > 0.0) {
                    targetReagMoles = batchModel.getStoicWeightAmount().getValueInStdUnitsAsDouble() / batchModel.getStoicMolecularWeightAmount().doubleValue();
                    amts.add(batchModel.getStoicMolecularWeightAmount());
                    amts.add(batchModel.getStoicWeightAmount());
                    if (batchModel.getStoicPurityAmount().doubleValue() < 100d && batchModel.getStoicPurityAmount().doubleValue() > 0.0) {
                        result = result * (batchModel.getStoicPurityAmount().doubleValue() / 100);
                        amts.add(batchModel.getStoicPurityAmount());
                    }
                }
                if (targetReagMoles > 0.0) {
                    // (targetReagMoles / sourceMoles) = (targetReag.getRxnEquivs() / sourceReag.getRxnEquivs())
                    // RxnEquivsAmount is scalar, hence no need for std units.
                    double testResult = (targetReagMoles / sourceMoles) * sourceRxnEquivs;
                    if (testResult >= 0.0)
                        result = testResult;
                    // RxnEquivs is a scalar Unit, though Mole amounts are currently only mMole amounts.
                    // Hence we can simply set the result, but it is safer tu use InStdUnits() method
                    // as there is noise of adding mole amounts in the future.
                    amts.add(limtReag.getStoicMoleAmount());
                    amts.add(limtReag.getStoicRxnEquivsAmount());
                    batchModel.getStoicRxnEquivsAmount().setValueInStdUnits(result, true);
                    batchModel.applySigFigRules(batchModel.getStoicRxnEquivsAmount(), amts);
                }
            }
        }
    }


    // Method to calc rxnEquivs for Reactants batches
    private void recalculateRxnEquivsBasedOnLimitingReagentMoles(StoicModelInterface limitingAB) {
        if (limitingAB != null && limitingAB.isStoicLimiting()) {
            // Need nto calculated Amoutns for SOLVENTS
            getReagentBatches().stream().filter(b -> !b.equals(limitingAB) && b.isAutoCalcOn()).forEach(b -> {
                if (limitingAB.getStoicRxnEquivsAmount().doubleValue() > 0.0
                        && limitingAB.getStoicMoleAmount().doubleValue() > 0.0) {
                    // Need nto calculated Amoutns for SOLVENTS
                    if (!b.getStoicReactionRole().equals(BatchType.SOLVENT.toString()))
                        recalculateRxnEquivsForBatch(limitingAB, b);
                } else if (b.getStoicMoleAmount().doubleValue() > 0.0) {
                    b.getStoicMoleAmount().reset();
                }
            });
        }
    }

    /**
     * @return a List of Reagent batches for the displayed reaction step.
     * The list includes reactants, reagents, solvents and starting
     * materials where starting materials are a discontinued type from the Chemistry Workbook.
     */
    private List<StoicModelInterface> getReagentBatches() {
        return getBatches();
    }

    /**
     * @return a list of StoicModelInterface object: BatchesList and BatchModel
     * that are of a product type: ACTUAL or INTENDED for this reaction step
     */
    private List<StoicModelInterface> getProductBatches() {
        List<StoicModelInterface> result;
        result = getIntendedProductBatches();
        result.sort(new ComparatorStoicAdditionOrder());
        return result;
    }

    //all stoic table
    private List<StoicModelInterface> getBatches() {
        LOGGER.debug("StoichCalculator.getBatches().enter");
        ArrayList<StoicModelInterface> stoicList = new ArrayList<>();
        if (rxnStepModel != null) {
            stoicList.addAll(rxnStepModel.getBatchesFromStoicBatchesList());
            return stoicList;
        }
        return stoicList;
    }

    private List<StoicModelInterface> getIntendedProductBatches() {
        ArrayList<StoicModelInterface> result = new ArrayList<>();
        if (rxnStepModel != null) {
            result.addAll(rxnStepModel.getProductBatches().stream().filter(productBatchModel -> productBatchModel.getBatchType() == BatchType.INTENDED_PRODUCT).collect(Collectors.toList()));
        }
        return result;
    }

    private List<StoicModelInterface> getActualProductBatches() {
        ArrayList<StoicModelInterface> result = new ArrayList<>();
        if (rxnStepModel != null) {
            for (ProductBatchModel productBatchModel : rxnStepModel.getProductBatches()) {
                if (productBatchModel.getBatchType() == BatchType.ACTUAL_PRODUCT) {
                    result.add(productBatchModel);
                }
            }
        }
        return result;
    }

}
