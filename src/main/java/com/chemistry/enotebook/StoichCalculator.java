package com.chemistry.enotebook;

import com.chemistry.enotebook.domain.*;
import com.chemistry.enotebook.experiment.businessmodel.stoichiometry.ComparatorStoicAdditionOrder;
import com.chemistry.enotebook.experiment.common.units.UnitType;
import com.chemistry.enotebook.experiment.datamodel.batch.BatchType;
import com.chemistry.enotebook.experiment.utils.BatchUtils;
import com.chemistry.enotebook.experiment.utils.CeNNumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//this class plays the role of StoichiometeryModel in 1.1
//Also look at ReactionStepModelUtils.java
public class StoichCalculator {

    private static final Log log = LogFactory.getLog(StoichCalculator.class);
    private final ReactionStepModel rxnStepModel;

//    private String pageType; // Parallel/Med_chem etc

    public StoichCalculator(ReactionStepModel mrxnStepModel) {
        this.rxnStepModel = mrxnStepModel;
    }

    /**
     * Use to have the ReactionStep's Stoichiometry reevaluated Resets limiting reagent if one can't be found.
     */
    public void recalculateStoich() {
        log.debug("StoichCalculator.recalculateStoich().enter");
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
        log.debug("StoichCalculator.recalculateStoich().exit");
    }

    public void recalculateStoichBasedOnBatch(StoicModelInterface rb, boolean calcMolesOnly) {
        log.debug("StoichCalculator.recalculateStoichBasedOnBatch().enter");
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
                if (CeNNumberUtils.doubleEquals(limitingReag.getStoicRxnEquivsAmount().GetValueInStdUnitsAsDouble(), 0.0,
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

            // if rb data is meaningfull then display EQ value otherwise - don't display
//			rb.getStoicRxnEquivsAmount().setCanBeDisplayed(!isReactionEquivMean	ingless(rb));
        }
        log.debug("StoichCalculator.recalculateStoichBasedOnBatch().exit");
    }

    private void recalculateProductsBasedOnStoich() {
        log.debug("StoichCalculator.recalculateProductsBasedOnStoich().enter");
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
        log.debug("StoichCalculator.recalculateProductsBasedOnStoich().exit");
    }

    /**
     * If only Volume is set for the batch then don't display reaction eq value
     */
    private boolean isReactionEquivMeaningless(StoicModelInterface rb) {
        // if only Volume is set for it then don't display eq value
        return !rb.getStoicVolumeAmount().isCalculated() && rb.getStoicMoleAmount().isCalculated() &&
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
        log.debug("StoichCalculator.recalculateSolventAmounts().enter");
        if (limitingReagent == null)
            return;
        AmountModel molarAmount = rb.getStoicMolarAmount();
        AmountModel volumeAmount = rb.getStoicVolumeAmount();
        ArrayList<AmountModel> volumeAmountList = getVolumeAmountsForAllSolvents();
        if (molarAmount.GetValueInStdUnitsAsDouble() == 0.0 && volumeAmount.GetValueInStdUnitsAsDouble() == 0.0)
            return;

        List<StoicModelInterface> reagents = getReagentBatches();
        for (int i = 0; i < reagents.size(); i++) {
            StoicModelInterface b = reagents.get(i);
            // if solvant go ahead
            if (b.getStoicReactionRole().equals(BatchType.SOLVENT.toString())) {
                // if this batch is the editing batch
                if (b.equals(rb)) {
                    if (volumeAmount.isCalculated()) {
                        // Case for user entry for Molar amount
                        if (!molarAmount.isCalculated() && molarAmount.GetValueInStdUnitsAsDouble() != 0) {
                            recalculateVolumeForSolvent(limitingReagent.getStoicMoleAmount(), rb, volumeAmountList);
                            molarAmount = rb.getStoicMolarAmount();
                            updateMolarAmountsForSolvent(molarAmount.GetValueInStdUnitsAsDouble(), molarAmount.getSigDigits());
                            molarAmount.setCalculated(false);
                            rb.setPreviousMolarAmount(molarAmount);
                        }
                    } else if (molarAmount.isCalculated()) {
                        if (molarAmount.GetValueInStdUnitsAsDouble() == 0 || areAllVolumesUserEnteredForSovents())
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
                        if (tempMolarAmt.GetValueInStdUnitsAsDouble() != molarAmount.GetValueInStdUnitsAsDouble()) {
                            // Molarity entered by the user
                            updateMolarAmountsForSolvent(rb.getStoicMolarAmount().GetValueInStdUnitsAsDouble(), rb
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
                        } // end of if-else started with if(isCurrentCalcForMolarityChange)
                    } // end of if-else started with if (volumeAmount.isCalculated())
                }// end of if(b.equals(rb))
            }// end of if (b.getType().equals(BatchType.SOLVENT))
        }// end of for loop
        log.debug("StoichCalculator.recalculateSolventAmounts().exit");
    }

    private void recalculateAmountsForBatch(StoicModelInterface limtReag, StoicModelInterface targetReag, boolean calcMolesOnly) {
        log.debug("StoichCalculator.recalculateAmountsForBatch().enter");
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
        log.debug("StoichCalculator.recalculateAmountsForBatch().exit");
    }

    private void resetRxnEquivs(StoicModelInterface ab) {
        if (ab.getStoicRxnEquivsAmount().doubleValue() == 0 && ab.getStoicRxnEquivsAmount().isCalculated()) {
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
        // todo check, guid key equality was deleted
        rxnStepModel.getStoicElementListInTransactionOrder().stream().filter(b -> !b.equals(exceptForThisReagent)).forEach(b -> { // todo check, guid key equality was deleted
            b.setStoicLimiting(false);
        });
    }

    /**
     * Recalculates Moles for all batches in this ReactionStep given the limiting Reagent. Use when limiting reagent is set, or the
     * amount is changed in some way.
     */
    private void recalculateMolesBasedOnLimitingReagent(StoicModelInterface limitingAB) {
        log.debug("StoichCalculator.recalculateMolesBasedOnLimitingReagent().enter");
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
                // So in effect you are saying that if the volume of the limiting reagent is zero you don't
                // want to permit any reagent from being updated by values in the limiting reagent.
                // That is an incorrect assumption as limiting reagent can be dry or neat.
                // I believe this check-in will break our calculations when volume of limiting reagent is zero.
                // Try again.
                if (CeNNumberUtils.doubleEquals(limitingAB.getStoicVolumeAmount().doubleValue(), 0.0) && CeNNumberUtils.doubleEquals(limitingAB.getStoicWeightAmount().doubleValue(), 0.0)) {
                    //nothing
                    if (b.isStoicLimiting()) {
                        AmountModel am = b.getStoicMoleAmount();
                        AmountModel tmpAmt = new AmountModel(am.getUnitType());
                        tmpAmt.deepCopy(am);
                        limitingAB.setStoicMoleAmount(tmpAmt);
                    }
                } else if (b.getStoicRxnEquivsAmount().GetValueInStdUnitsAsDouble() > 0.0 &&
                        (b.getStoicMoleAmount().doubleValue() > 0.0 ||
                                CeNNumberUtils.doubleEquals(b.getStoicMoleAmount().doubleValue(), 0.0) &&
                                        b.getStoicVolumeAmount().doubleValue() > 0.0) || limitingAB.isStoicLimiting()) {
                    // Need to not calculate Amounts for SOLVENTS
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
        log.debug("StoichCalculator.recalculateMolesBasedOnLimitingReagent().exit");
    }

    private void recalculateMolarAmountForSolvent(AmountModel limitingMole) {
        log.debug("StoichCalculator.recalculateMolarAmountForSolvent().enter");
        ArrayList<AmountModel> amts = new ArrayList<>();
        ArrayList<AmountModel> amountsList = getVolumeAmountsForAllSolvents();
        AmountModel tempAmount;
        double totalVolume = 0;
        // adding up the total volume
        for (AmountModel anAmountsList : amountsList) {
            tempAmount = anAmountsList;
            totalVolume += tempAmount.GetValueInStdUnitsAsDouble();
        }
        amts.addAll(amountsList);
        amts.add(limitingMole);
        // Molarity calculation
        double molarity = limitingMole.GetValueInStdUnitsAsDouble() / totalVolume;
        updateMolarAmountsForSolvent(molarity, CeNNumberUtils.getSmallestSigFigsFromAmountModelList(amts));
        amts.clear();
        log.debug("StoichCalculator.recalculateMolarAmountForSolvent().exit");
    }

    private void findAndRecalculateVolumeForSolvents(List<StoicModelInterface> reagents, AmountModel limitingMoleAmount, AmountModel currentVolumeAmount) {

        // update the calculated volume to current value.
        for (StoicModelInterface batch : reagents) {
            if (batch.getStoicReactionRole().equals(BatchType.SOLVENT.toString())) {
                AmountModel tempVolumeAmt = batch.getStoicVolumeAmount();
                if (!tempVolumeAmt.equals(currentVolumeAmount)) {
                    if (tempVolumeAmt.isCalculated()) {
                        recalculateVolumeForSolvent(limitingMoleAmount, batch, getVolumeAmountsForAllSolvents());
                    }// end of if(volumeAmount.isCalculated())
                } // end of if(!reagents.get(k).equals(volumeAmount))
            }// end of if(b.getType().equals(BatchType.SOLVENT)
        } // end of for loop
    }

    private void updateActualProductTheoAmounts(StoicModelInterface limitingReag) {
        log.debug("StoichCalculator.updateActualProductTheoAmounts().enter");
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
        log.debug("StoichCalculator.updateActualProductTheoAmounts().exit");
    }

    private void recalculateProductAmounts(StoicModelInterface limitingReag, StoicModelInterface prodBatch) {
        log.debug("StoichCalculator.recalculateProductAmounts().enter");
        if (limitingReag != null && prodBatch.isAutoCalcOn()) {
            // if moleAmount are not = limiting moleAmount and not set by the user, set it.
            // send to recalc based on moleAmount. Need to be sure that weightAmount isn't
            // edited by the user either.
            if (!prodBatch.getStoicRxnEquivsAmount().isCalculated() ||
                    (prodBatch.getStoicWeightAmount().isCalculated() && prodBatch.getStoicMoleAmount().isCalculated()
                            && !limitingReag.getStoicMoleAmount().equals(prodBatch.getStoicMoleAmount()))) {
                // Need to set calc flag regardless of limitingReag state.
                AmountModel amtTemp = (AmountModel) limitingReag.getStoicMoleAmount().deepClone();
                double targetMoles = amtTemp.doubleValue() * prodBatch.getStoicRxnEquivsAmount().GetValueInStdUnitsAsDouble();
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
        log.debug("StoichCalculator.recalculateProductAmounts().exit");
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
                molarModel.SetValueInStdUnits(value);
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
            if (b.getStoicReactionRole().equals(BatchType.SOLVENT.toString())) {
                if (b.getStoicVolumeAmount().isCalculated())
                    return false;
            }
        }
        return true;
    }

    private void recalculateVolumeForSolvent(AmountModel limitingMole, StoicModelInterface rb, ArrayList<AmountModel> volumeAmounts) {
        ArrayList<AmountModel> amts = new ArrayList<>();
        AmountModel tempVolumeAmt;
        double tempMolarAmtValue = rb.getStoicMolarAmount().GetValueInStdUnitsAsDouble();
        double calcVolume;
        amts.add(limitingMole);
        amts.add(rb.getStoicMolarAmount());

        if (tempMolarAmtValue > 0) {
            calcVolume = limitingMole.GetValueInStdUnitsAsDouble() / tempMolarAmtValue;
        } else
            return;
        // CalcVolume need to be subtracted with volumes of other solvents
        for (AmountModel volumeAmount : volumeAmounts) {
            tempVolumeAmt = volumeAmount;
            if (!tempVolumeAmt.equals(rb.getStoicVolumeAmount())) {
                calcVolume -= tempVolumeAmt.GetValueInStdUnitsAsDouble();
                amts.add(tempVolumeAmt);
                if (tempVolumeAmt.GetValueInStdUnitsAsDouble() != 0)
                    tempVolumeAmt.setCalculated(false);
            }
        }
        AmountModel actaulVolumeAmt = rb.getStoicVolumeAmount();
        AmountModel volumeAmt = (AmountModel) actaulVolumeAmt.deepClone();
        volumeAmt.SetValueInStdUnits(calcVolume, true);
        volumeAmt.setSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
        rb.setStoicVolumeAmount(volumeAmt);
        amts.clear();
    }

    private void applySigDigitsToAllSolventMolarites(AmountModel molarAmt) {
        ArrayList<AmountModel> amountsList = getMolarAmountsForAllSolvents();
        amountsList.stream().filter(tempAmt -> !tempAmt.equals(molarAmt)).forEach(tempAmt -> {
            tempAmt.setSigDigits(molarAmt.getSigDigits());
        });
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
            // ??? should we get value in std unit or the unit user selected ? molAmt.GetValueInStdUnitsAsDouble()
            double limitingMoles = limtReag.getStoicMoleAmount().GetValueInStdUnitsAsDouble();
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
                        resetRxnEquivs(batchModel); // targetReag.getRxnEquivsAmount().setValue("1.00", true);
                    }
                    // (targetMoles * ab.getRxnEquivs()) = (limitingMoles * limitingRxnEquivs())
                    double targetMoles = limitingMoles * (batchModel.getStoicRxnEquivsAmount().doubleValue() / limitingRxnEquivs);
                    amts.add(limtReag.getStoicMoleAmount());
                    amts.add(batchModel.getStoicRxnEquivsAmount());
                    amts.add(limtReag.getStoicRxnEquivsAmount());
                    // Do not do this as this calc below is wrong by 1/x!
                    // double targetMoles = limitingMoles * limitingRxnEquivs / targetReag.getRxnEquivsAmount().doubleValue();

                    // to trigger calc of weight ( recalcAmount() method )
                    AmountModel molModelActual = batchModel.getStoicMoleAmount();
                    AmountModel molModel = (AmountModel) molModelActual.deepClone();
                    // molModel.setValue(targetMoles);
                    // set same unit as Limiting Reagent's
                    // molModel.setUnit(limtReag.getStoicMoleAmount().getUnit());
                    // molModel.setCalculated(true);
                    molModel.SetValueInStdUnits(targetMoles, true);
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
                    resetRxnEquivs(targetReag); // targetReag.getRxnEquivsAmount().setValue("1.00", true);
                }
                // (targetMoles * ab.getRxnEquivs()) = (limitingMoles * limitingRxnEquivs())
                double targetMoles = limitingMoles * (targetReag.getStoicRxnEquivsAmount().doubleValue() / limitingRxnEquivs);
                amts.add(limtReag.getStoicMoleAmount());
                amts.add(targetReag.getStoicRxnEquivsAmount());
                amts.add(limtReag.getStoicRxnEquivsAmount());
                // Do not do this as this calc below is wrong by 1/x!
                // double targetMoles = limitingMoles * limitingRxnEquivs / targetReag.getRxnEquivsAmount().doubleValue();

//				AmountModel molModelActual = targetReag.getStoicMoleAmount();
                AmountModel molModel = (AmountModel) targetReag.getStoicMoleAmount().deepClone();
                // molModel.setValue(targetMoles);
                // set same unit as Limiting Reagent's
                // molModel.setUnit(limtReag.getStoicMoleAmount().getUnit());
                // molModel.setCalculated(true);
                // ??Issue is whether to set in std units or in units that of limReag's
                molModel.SetValueInStdUnits(targetMoles, true);
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

            double sourceMoles = limtReag.getStoicMoleAmount().GetValueInStdUnitsAsDouble();
            double sourceRxnEquivs = limtReag.getStoicRxnEquivsAmount().doubleValue();
            if (targetReagent instanceof BatchesList<?>) {
                BatchesList<MonomerBatchModel> blist = (BatchesList<MonomerBatchModel>) targetReagent;
                if (blist.getBatchModels() == null)
                    return;

                for (MonomerBatchModel batchModel : blist.getBatchModels()) {
                    double targetReagMoles = batchModel.getStoicMoleAmount().GetValueInStdUnitsAsDouble();

                    ArrayList<AmountModel> amts = new ArrayList<>();
                    if (targetReagMoles > 0.0) {
                        amts.add(batchModel.getStoicMoleAmount());
                    }
                    // Make sure targetReagMoles > 0
                    if (targetReagMoles == 0.0 && batchModel.getStoicMolecularWeightAmount().doubleValue() > 0.0) {
                        targetReagMoles = batchModel.getStoicWeightAmount().GetValueInStdUnitsAsDouble() / batchModel.getStoicMolecularWeightAmount().doubleValue();
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
                        // double testResult = (sourceMoles * sourceRxnEquivs) / targetReagMoles;
                        if (testResult >= 0.0)
                            result = testResult;
                        // RxnEquivs is a scalar Unit, though Mole amounts are currently only mMole amounts.
                        // Hence we can simply set the result, but it is safer tu use InStdUnits() method
                        // as there is noise of adding mole amounts in the future.
                        amts.add(limtReag.getStoicMoleAmount());
                        amts.add(limtReag.getStoicRxnEquivsAmount());
                        batchModel.getStoicRxnEquivsAmount().SetValueInStdUnits(result, true);
                        batchModel.applySigFigRules(batchModel.getStoicRxnEquivsAmount(), amts);
                    }
                }//for each batch
            } else {
                MonomerBatchModel batchModel = (MonomerBatchModel) targetReagent;
                double targetReagMoles = batchModel.getStoicMoleAmount().GetValueInStdUnitsAsDouble();

                ArrayList<AmountModel> amts = new ArrayList<>();
                if (targetReagMoles > 0.0) {
                    amts.add(batchModel.getStoicMoleAmount());
                }
                // Make sure targetReagMoles > 0
                if (targetReagMoles == 0.0 && batchModel.getStoicMolecularWeightAmount().doubleValue() > 0.0) {
                    targetReagMoles = batchModel.getStoicWeightAmount().GetValueInStdUnitsAsDouble() / batchModel.getStoicMolecularWeightAmount().doubleValue();
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
                    // Doing this is wrong!
//					double testResult = (sourceMoles * sourceRxnEquivs) / targetReagMoles;
                    if (testResult >= 0.0)
                        result = testResult;
                    // RxnEquivs is a scalar Unit, though Mole amounts are currently only mMole amounts.
                    // Hence we can simply set the result, but it is safer tu use InStdUnits() method
                    // as there is noise of adding mole amounts in the future.
                    amts.add(limtReag.getStoicMoleAmount());
                    amts.add(limtReag.getStoicRxnEquivsAmount());
                    batchModel.getStoicRxnEquivsAmount().SetValueInStdUnits(result, true);
                    batchModel.applySigFigRules(batchModel.getStoicRxnEquivsAmount(), amts);
                }
            }
        }
    }

    /**
     * This method adds a ProductBatchModel to products list if there are monomer batches and no INTENDED products in a ReactionStepModel
     * This is applicable for non-Parallel exps only
     */
    private void addProductIfNeeded() {
//        if (this.pageType.equals(CeNConstants.PAGE_TYPE_PARALLEL)) return;

        if (getProductBatches().size() == 0 &&
                getReactantBatches().size() >= 1) {
            createIntendedProduct(getProductBatches().size());  // Ignoring return value.
        }
    }

    private ProductBatchModel createIntendedProduct(int additionOrder) {
        ProductBatchModel result = null;
        try {
            result = new ProductBatchModel();
            result.setBatchType(BatchType.INTENDED_PRODUCT);
            resetRxnEquivs(result);
            result.setIntendedBatchAdditionOrder(additionOrder);
        } catch (Exception e) {
            // should never throw this as we are in control of the batch type.
            // development only error.
            log.error("Failed to create Intended Product.\n" + e.toString(), e);
        }
        return result;
    }

    /**
     * @return List of strings representing the precursors of each reactant and
     * each reactant in that structurally contribute to the final product.
     */
    private ArrayList<String> getPreCursorsForReaction() {
        ArrayList<String> tmpList = new ArrayList<>();
        List<String> smPrecursors;
        // Process all the Reagents to ensure the order is correct
        for (StoicModelInterface stoicModelInterface : getReactantBatches()) {
            // Does this reactant have precursors?  If so put them into
            // the precursor list instead of the reactant.
            MonomerBatchModel rb = (MonomerBatchModel) stoicModelInterface;
            smPrecursors = rb.getPrecursors();
            if (smPrecursors != null && smPrecursors.size() > 0) {
                smPrecursors.stream().filter(testStr -> tmpList.indexOf(testStr) < 0).forEach(tmpList::add);
            }
            // clean up.
            assert smPrecursors != null;
            smPrecursors.clear();
        }

        return tmpList;
    }

    /**
     * This method updates the precursor info for all Products.
     * This is only required for MedChem/Conception. Parallel experiment precurosrs are computed in different way.
     *
     * @throws IllegalArgumentException if it encounters anything but ProductBatchModel when calling
     *                                  getIntendedProductBatches()
     */
    private void updateIntendedProductPrecursors() {
        ArrayList<String> precursors = getPreCursorsForReaction();
        for (StoicModelInterface stoichItem : getIntendedProductBatches()) {
            if (stoichItem instanceof ProductBatchModel) {
                ((ProductBatchModel) stoichItem).setPrecursors(precursors);
            } else if (stoichItem instanceof BatchesList<?>) {
                throw new IllegalArgumentException("Parallel Experiments cannot use updateIntendedProductPrecursors.");
            } else {
                throw new IllegalArgumentException("Non-ProductBatchModel found when calling getIntendedProductBatches().");
            }
        }

    }

    /*
     * This method triggers calc of Moles based on user entered nEquivs and also calc of nEquiv based on user entered Moles and
     * other calcs like yield and theoretical Wt for a particular product batch.(in Products Table ) nMoles = nEquiv *
     * limitingReagentMoles
     */
    public void recalculateProductMolesandEquivs(StoicModelInterface prodBatch) {
        StoicModelInterface limitingReag = findLimitingReagent();

        if (limitingReag != null) {
            addProductIfNeeded(); // used to keep a place marker for stoich products if there isn't one
            // allows calcs to be setup such that the user can see the effect of
            // their changes.

            updateIntendedProductPrecursors();

            // if intd prod nMoles is set zero then set its value back to limiting
            // reagent nMoles and equivs as 1.0
            if (prodBatch.getStoicWeightAmount().GetValueInStdUnitsAsDouble() == 0.0) {
                // set nMoles
                AmountModel amtTemp = (AmountModel) limitingReag.getStoicMoleAmount().deepClone();
                amtTemp.setCalculated(true);
                // Causes batch to recalculate amounts based on Mole change.
                prodBatch.setStoicMoleAmount(amtTemp);

                if (limitingReag.shouldApplySigFigRules())
                    prodBatch.applyLatestSigDigits(amtTemp.getSigDigits());
                else
                    prodBatch.applyLatestSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);

                // set nEquivs
                AmountModel amtTemp2 = (AmountModel) limitingReag.getStoicRxnEquivsAmount().deepClone();
                amtTemp2.setCalculated(true);
                prodBatch.setStoicRxnEquivsAmount(amtTemp2);

                prodBatch.recalcAmounts();

            } else if (prodBatch.isAutoCalcOn()) {
                AmountModel amtTemp = (AmountModel) limitingReag.getStoicMoleAmount().deepClone();
                // prodBatch.setTheoreticalMoleAmount(amtTemp);

                // if nEquiv set by the user.recalc moleAmount based on nEquiv and limiting reagent nMoles.
                if (prodBatch.getStoicMoleAmount().isCalculated() && !prodBatch.getStoicRxnEquivsAmount().isCalculated()) {
                    // calc new mole amount
                    double newValue = BatchUtils.calcMolesWithEquivalents(amtTemp, prodBatch.getStoicRxnEquivsAmount());
                    amtTemp.setValue(newValue);
                    amtTemp.setCalculated(true);

                    // Causes batch to recalculate amounts based on Mole change.
                    prodBatch.setStoicMoleAmount(amtTemp);
                    if (limitingReag.shouldApplySigFigRules())
                        prodBatch.applyLatestSigDigits(amtTemp.getSigDigits());
                    else
                        prodBatch.applyLatestSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
                }
                // if moleAmount set by the user.
                // recalc nEquiv based on moleAmount and limiting reagent nMoles.
                else if (!prodBatch.getStoicMoleAmount().isCalculated() && prodBatch.getStoicRxnEquivsAmount().isCalculated()) {
                    // calc new Equiv amount
                    double newValue = BatchUtils.calcEquivalentsWithMoles(prodBatch.getStoicMoleAmount(), amtTemp);
                    amtTemp.setValue(newValue);
                    amtTemp.setCalculated(true);

                    // Causes batch to recalculate amounts based on Equivs change.
                    prodBatch.setStoicRxnEquivsAmount(amtTemp);
                    if (limitingReag.shouldApplySigFigRules())
                        prodBatch.applyLatestSigDigits(amtTemp.getSigDigits());
                    else
                        prodBatch.applyLatestSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
                }

                prodBatch.recalcAmounts(); // Updates the theo weight and yield
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
        return getBatches(BatchType.REACTANT_ORDINAL |
                BatchType.REAGENT_ORDINAL |
                BatchType.SOLVENT_ORDINAL |
                BatchType.START_MTRL_ORDINAL);
    }

    /**
     * @return a List of Reactant batches for the displayed reaction step.
     * Only Reactant batches are returned. Use getReagents() if
     * you need all reagent types.
     */
    private List<StoicModelInterface> getReactantBatches() {
        ArrayList<StoicModelInterface> result = new ArrayList<>();
        if (rxnStepModel != null) {
            result.addAll(rxnStepModel.getProductBatches().stream().filter(productBatchModel -> productBatchModel.getBatchType() == BatchType.REACTANT).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * @return a list of StoicModelInterface object: BatchesList and BatchModel
     * that are of a product type: ACTUAL or INTENDED for this reaction step
     */
    private List<StoicModelInterface> getProductBatches() {
        List<StoicModelInterface> result;
//        if (this.pageType.equals(CeNConstants.PAGE_TYPE_PARALLEL)) {
//            result = getBatches(BatchType.ACTUAL_PRODUCT_ORDINAL);
//        } else {
//        result = getBatches(BatchType.INTENDED_PRODUCT_ORDINAL);
        result = getIntendedProductBatches();
//        }
        Collections.sort(result, new ComparatorStoicAdditionOrder());
        return result;
    }

    //all stoic table
    private List<StoicModelInterface> getBatches(int batchTypes) {
        log.debug("StoichCalculator.getBatches().enter");
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
//      return getBatches(BatchType.ACTUAL_PRODUCT_ORDINAL);
    }

}
