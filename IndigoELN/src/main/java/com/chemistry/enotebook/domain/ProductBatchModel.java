package com.chemistry.enotebook.domain;

import com.chemistry.enotebook.experiment.common.units.Unit2;
import com.chemistry.enotebook.experiment.common.units.UnitType;
import com.chemistry.enotebook.experiment.datamodel.batch.BatchType;
import com.chemistry.enotebook.experiment.datamodel.common.Amount2;
import com.chemistry.enotebook.experiment.utils.BatchUtils;
import com.chemistry.enotebook.experiment.utils.CeNNumberUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductBatchModel extends BatchModel {

    private static final long serialVersionUID = 213824001208174426L;

    // theo amounts were added because they are valid search criteria.
    // therefore the data must exist in the database for indexing.
    // The values will be as below unless intendedBatch != null
    // Also the IntProd's weightAmount is ActualProd's theoWeightAmount
    // IntProd's moleAmount is ActualProd's theoMoleAmount
    private AmountModel theoreticalYieldPercentAmount = new AmountModel(UnitType.SCALAR, "-1.0"); // -1 means not set.
    private AmountModel theoreticalWeightAmount = new AmountModel(UnitType.MASS);
    private AmountModel theoreticalMoleAmount = new AmountModel(UnitType.MOLES);

    private int lastUpdatedProductType = UPDATE_TYPE_MOLES;

    public ProductBatchModel() {
        super.setBatchType(BatchType.INTENDED_PRODUCT);
    }

    public ProductBatchModel(AmountModel molecularWeightAmount, AmountModel moleAmount, AmountModel weightAmount, AmountModel volumeAmount, AmountModel densityAmount, AmountModel molarAmount, AmountModel purityAmount, AmountModel rxnEquivsAmount, boolean limiting, BatchType batchType, AmountModel totalVolume, AmountModel totalWeight, AmountModel totalMolarity, AmountModel theoreticalYieldPercentAmount, AmountModel theoreticalWeightAmount, AmountModel theoreticalMoleAmount) {
        super(molecularWeightAmount, moleAmount, weightAmount, volumeAmount, densityAmount, molarAmount, purityAmount, rxnEquivsAmount, limiting, batchType, totalVolume, totalWeight, totalMolarity);
        this.theoreticalMoleAmount = theoreticalMoleAmount;
        this.theoreticalWeightAmount = theoreticalWeightAmount;
        this.theoreticalYieldPercentAmount = theoreticalYieldPercentAmount;
    }

    /**
     * @param intendedBatchAdditionOrder the intendedBatchAdditionOrder to set
     */
    public void setIntendedBatchAdditionOrder(int intendedBatchAdditionOrder) {
        super.setTransactionOrder(intendedBatchAdditionOrder);
        this.modelChanged = true;
    }

    public AmountModel getTheoreticalMoleAmount() {
        return theoreticalMoleAmount;
    }

    public void setTheoreticalMoleAmount(AmountModel mtheoreticalMoleAmount) {
        if (!theoreticalMoleAmount.equals(mtheoreticalMoleAmount)) {
            boolean oldCalcFlag = theoreticalMoleAmount.isCalculated();
            theoreticalMoleAmount.deepCopy(mtheoreticalMoleAmount);
            theoreticalMoleAmount.setCalculated(oldCalcFlag);
            updateTheoreticalWeightAmount();
            setModified(true);
        }
    }

    public AmountModel getTheoreticalWeightAmount() {
        return theoreticalWeightAmount;
    }

    /**
     * @param theoreticalWeightAmount
     *            the theoreticalWeightAmount to set
     */
    public void setTheoreticalWeightAmount(AmountModel theoreticalWeightAmount) {
        this.theoreticalWeightAmount = theoreticalWeightAmount;
        this.modelChanged = true;
    }

    /**
     * Triggers a recalculation of amounts based on last valid entry.
     * <p>
     * For product batches this also recalculates TheoreticalYieldPercent
     */
    public void recalcAmounts() {
        // needs to be tested: Added isEditable() to prevent updates unnecessarily.
        // The test needs to be such that there were items that used to be calculated on the fly.
        if (autoCalcOn && !inCalculation) {
            //As per 1.1 calc and also trigger weight,volume amounts calc
            super.recalcAmounts();
            //Trigger totalXXXAmounts calc. this can calc few values twice.
            recalcTotalAmounts();
            // Need to reestablish Yield if things changed
            inCalculation = true;
            updateTheoreticalWeightAmount();
            if (theoreticalWeightAmount.doubleValue() > 0.0) { // vb 5/9
                // substituted
                // total weight
                // for weight
                // amount
                double yield = 100 * (this.getTotalWeight()
                        .GetValueInStdUnitsAsDouble() / theoreticalWeightAmount
                        .GetValueInStdUnitsAsDouble());
                ArrayList<AmountModel> amts = new ArrayList<>();
                amts.add(getWeightAmount());
                amts.add(getTheoreticalWeightAmount());
                getTheoreticalYieldPercentAmount().setSigDigits(
                        CeNNumberUtils
                                .getSmallestSigFigsFromAmountModelList(amts));
                // applySigFigRules(getTheoreticalYieldPercentAmount(), amts);
                amts.clear();
                theoreticalYieldPercentAmount.setValue(yield);
            } else if (theoreticalWeightAmount.doubleValue() == 0.0) {
                // If the theoreticalWeightAmount is 0 then yeild % is 0
                theoreticalYieldPercentAmount.reset();
            }
            inCalculation = false;
        }
    }

    private void updateTheoreticalWeightAmount() {
        if (theoreticalWeightAmount.isCalculated() && getMolWgt() > 0.0) {
            theoreticalWeightAmount.setUnit(getWeightAmount().getUnit());
            getTotalWeight().setUnit(getWeightAmount().getUnit());

            ArrayList<AmountModel> amts = new ArrayList<>();
            amts.add(getTheoreticalMoleAmount());
            amts.add(getMolecularWeightAmount());
            theoreticalWeightAmount.setSigDigits(CeNNumberUtils
                    .getSmallestSigFigsFromAmountModelList(amts));
            amts.clear();
            theoreticalWeightAmount.SetValueInStdUnits(theoreticalMoleAmount
                    .GetValueInStdUnitsAsDouble()
                    * getMolWgt());
        }
    }

    protected List<AmountModel> getCalculatedAmounts() {
        ArrayList<AmountModel> result = new ArrayList<>();
        result.add(getTheoreticalMoleAmount()); // Set by limiting reagent or by
        // user.
        result.add(getTheoreticalWeightAmount()); // Set by limiting reagent.
        result.add(getTheoreticalYieldPercentAmount()); // Calulated from weight
        // amounts Theo and
        // Actual
        result.add(getMoleAmount()); // Set by limiting if intended type or
        // if actual is calculated from weight.
        result.add(getWeightAmount()); // Set by user or calc'd from moles.
        result.add(getVolumeAmount()); // Calculated in registration/submission
        // based on solvent used to dilute
        // sample.
        result.add(getPurityAmount()); // no single purity amount available as
        // of 1/17/2006 for intended or actual
        // products
        result.add(getRxnEquivsAmount()); // Use this to set initial sigdigits
        // = 3 by setting to 1.00 as
        // default.
        return result;
    }

    /**
     * Uses weights to find yeild. Is always calculated.
     *
     * @return -1.0 if no IntendedProduct is associated with this batch. -1.0 if
     * IntendedProduct's moleAmount.GetValueInStdUnits() == 0.0. 100.0 -
     * 0.0 value if there is an IntendedProduct to match.
     */
    public AmountModel getTheoreticalYieldPercentAmount() {
        return theoreticalYieldPercentAmount;
    }

    private AmountModel getTotalMolarAmount() {
        return super.getTotalMolarity();
    }

    public void setTotalMolarAmount(AmountModel totalMolarAmount) {
        if (totalMolarAmount != null
                && totalMolarAmount.GetValueInStdUnitsAsDouble() != 0.0) {
            if (totalMolarAmount.getUnitType().getOrdinal() == UnitType.MOLAR
                    .getOrdinal()) {
                // Check to see if it is a unit change
                if (getTotalMolarity() != null
                        && !getTotalMolarity().equals(totalMolarAmount)) {
                    boolean unitChange = BatchUtils.isUnitOnlyChanged(
                            getTotalMolarity(), totalMolarAmount);
                    getTotalMolarity().deepCopy(totalMolarAmount);
                    if (!unitChange) {
                        lastUpdatedProductType = BatchModel.UPDATE_TYPE_TOTAL_MOLARITY;
                        //This includes recalcTotalAmounts() as well
                        recalcAmounts();
                        setModified(true);
                    }
                }
            }
        } else {
            getTotalMolarity().setValue("0");
        }
    }

    // vb 2/2
    private void recalcTotalAmounts() {
        // Make sure we don't get into a loop!
        // And that we don't lose information on load.
        // if (!autoCalcOn || inCalculation)
        // return;

        ArrayList<AmountModel> amts = new ArrayList<>();
        inCalculation = true;
        // Check which value was set by hand: solid or liquid
        // Molar type as last updated not considered here because moles is
        // considered driver
        // when there is a tie in flags.
        if (lastUpdatedProductType == UPDATE_TYPE_TOTAL_VOLUME) { // && !
            // this.getTotalVolume().isCalculated())
            // {
            // We need to update moles and weight from volume
            // Molarity takes precedence over density
            if (this.getMolarAmount().doubleValue() > 0) {
                amts.add(this.getMolarAmount());
                amts.add(this.getTotalVolume());
                applySigFigRules(this.getMoleAmount(), amts);
                amts.clear(); // important to clear the amts list
                // Update mole amount
                // Std unit for molar is mMolar
                // mMoles = (mole/L) * mL
                this.getMoleAmount().SetValueInStdUnits(
                        this.getMolarAmount().GetValueInStdUnitsAsDouble()
                                * this.getTotalVolume()
                                .GetValueInStdUnitsAsDouble(), true);
                getMoleAmount().setCalculated(true);
                updateTotalWeightFromMoles();
            } else if (this.getDensityAmount().doubleValue() > 0) {
                // find governing sig figs
                amts.add(this.getTotalVolume());
                amts.add(this.getDensityAmount());
                applySigFigRules(this.getTotalWeight(), amts);
                amts.clear();// important to clear the amts list
                // mg = (mL * g/mL)/ (1000 mg/g)
                this.getTotalWeight().SetValueInStdUnits(1000 * this.getTotalVolume().GetValueInStdUnitsAsDouble()
                        * this.getDensityAmount().GetValueInStdUnitsAsDouble(), true);
                updateMolesFromTotalWeight();
            }
            updateTotalMolarity();
        } else if (lastUpdatedProductType == UPDATE_TYPE_TOTAL_WEIGHT) {
            updateMolesFromTotalWeight();
            // Now that the solids are straightened out, we can calc the liquid
            if (this.getMolarAmount().doubleValue() > 0) {
                // update volume
                amts.add(this.getMoleAmount());
                amts.add(this.getMolarAmount());
                if (this.getTotalVolume().isCalculated()
                        && this.getTotalWeight().isCalculated())
                    this.getTotalVolume().setSigDigits(
                            CeNNumberUtils.DEFAULT_SIG_DIGITS);
                else
                    applySigFigRules(this.getTotalVolume(), amts);
                amts.clear();// important to clear the amts list
                this.getTotalVolume().SetValueInStdUnits(
                        this.getMoleAmount().GetValueInStdUnitsAsDouble()
                                / this.getMolarAmount()
                                .GetValueInStdUnitsAsDouble(), true);
                this.getTotalVolume().setCalculated(true);
            }
            // Calculate total Molarity
            updateTotalMolarity();
        } else if (lastUpdatedProductType == UPDATE_TYPE_TOTAL_MOLARITY) {
            updateMolesFromTotalWeight();
            // Now that the solids are straightened out, we can calc the liquid
            if (this.getTotalMolarAmount().doubleValue() > 0) {
                // update volume
                amts.add(this.getMoleAmount());
                amts.add(this.getTotalMolarAmount());
                if (this.getTotalVolume().isCalculated()
                        && this.getTotalWeight().isCalculated())
                    this.getTotalVolume().setSigDigits(
                            CeNNumberUtils.DEFAULT_SIG_DIGITS);
                else
                    applySigFigRules(this.getTotalVolume(), amts);
                amts.clear();// important to clear the amts list
                this.getTotalVolume().SetValueInStdUnits(
                        this.getMoleAmount().GetValueInStdUnitsAsDouble()
                                / this.getTotalMolarAmount()
                                .GetValueInStdUnitsAsDouble(), true);
                this.getTotalVolume().setCalculated(true);
            } else if (this.getDensityAmount().doubleValue() > 0) {
                amts.add(this.getTotalWeight());
                amts.add(this.getDensityAmount());
                applySigFigRules(getTotalVolume(), amts);
                amts.clear();// important to clear the amts list

                // update volume from weight value: mg /(1000mg/g)* (g/mL) = mL
                getTotalVolume().SetValueInStdUnits(this.getTotalWeight().GetValueInStdUnitsAsDouble()
                        / (1000 * this.getDensityAmount().GetValueInStdUnitsAsDouble()), true);
            }
        }
        inCalculation = false;
    }

    private void updateTotalMolarity() {
        if (getMolarAmount().doubleValue() == 0.0
                && getMoleAmount().doubleValue() > 0.0
                && getTotalVolume().doubleValue() > 0.0) {
            double result = getMoleAmount().GetValueInStdUnitsAsDouble()
                    / getTotalVolume().GetValueInStdUnitsAsDouble();
            this.getTotalMolarAmount().SetValueInStdUnits(result, true);
            this.getTotalMolarAmount().setCalculated(true);
        }
    }

    /**
     * Do we apply sig fig rules to calculations?
     */
    public boolean shouldApplySigFigRules() {
        return (!getTotalWeight().isCalculated() || !getTotalVolume()
                .isCalculated());
    }

    /**
     * Applies SigFigs for the Amount Obeject. Checks if the standard sigfig
     * rules to be applied, which checks for any user edits on Measured Amounts
     * (Weight,Volume). else any other edit would lead to Default SigFig
     * application on the Amount.
     */
    public void applySigFigRules(Amount2 amt, List<AmountModel> amts) {
        if (shouldApplySigFigRules()) {
            amt.setSigDigits(CeNNumberUtils
                    .getSmallestSigFigsFromAmountModelList(amts));
        } else {
            if (shouldApplyDefaultSigFigs())
                amt.setSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
        }
    }

    /**
     *
     */
    public boolean shouldApplyDefaultSigFigs() {
        return (!this.getRxnEquivsAmount().isCalculated()
                || !this.getMoleAmount().isCalculated() || !this
                .getTotalMolarAmount().isCalculated());
    }

    private void updateTotalWeightFromMoles() {
        this.getTotalWeight().setCalculated(true);
        ArrayList<AmountModel> amts = new ArrayList<AmountModel>();
        // weight = weight/mole * moles
        // In this case moles are always mMoles and the default return of mole
        // amount is in mmoles
        // Hence when we setValue for weight we are doing so in mg and no
        // conversion is necessary.
        double result = getMolWgt()
                * getMoleAmount().GetValueInStdUnitsAsDouble();
        if (getPurityAmount().doubleValue() < 100d
                && getPurityAmount().doubleValue() > 0.0) {
            result = result / (getPurityAmount().doubleValue() / 100);
            amts.add(this.getMoleAmount());
            amts.add(this.getPurityAmount());
            amts.add(this.getMolecularWeightAmount());
        } else {
            amts.add(this.getMoleAmount());
            amts.add(this.getMolecularWeightAmount());
        }
        // Applies SignificantFigures to weightAmount
        applySigFigRules(this.getTotalWeight(), amts);
        amts.clear();

        // We just got the result for mg * purity),
        // Now we need to make sure the answer makes sense for the units of
        // weight currently being used.
        this.getTotalWeight().SetValueInStdUnits(result, true);
    }

    private void updateMolesFromTotalWeight() {
        double result = 0d;
        ArrayList<AmountModel> amts = new ArrayList<>();
        // weight std unit = mg; density std unit = mg/mmol
        // Weight = 9.82m/s^2 * mass
        if (getMolWgt() > 0.0) {
            // TODO: Warning. Using getMolWgt() in the future may not return
            // standard units. Make sure this changes if the
            // users ever get the opportunity to change units of
            // MolWt.
            result = this.getTotalWeight().GetValueInStdUnitsAsDouble()
                    / getMolWgt();
            amts.add(this.getTotalWeight());
            amts.add(this.getMolecularWeightAmount());
            // we just got the result for mg/mmole,
            // now we need to make sure the answer makes sense for the units of
            // weight currently being used.
            if (getPurityAmount().doubleValue() < 100d
                    && this.getPurityAmount().doubleValue() > 0.0) {
                result = result * (this.getPurityAmount().doubleValue() / 100);
                amts.add(this.getPurityAmount());
            }
        }
        applySigFigRules(this.getMoleAmount(), amts);
        Unit2 unit = this.getMoleAmount().getUnit();
        amts.clear();
        this.getMoleAmount().SetValueInStdUnits(result, true);
        this.getMoleAmount().setUnit(unit);
        this.getMoleAmount().setCalculated(true);
    }

    public void setTotalWeightAmount(AmountModel weight) {
        if (weight != null /*&& weight.GetValueInStdUnitsAsDouble() != 0.0*/) {
            if (weight.getUnitType().getOrdinal() == UnitType.MASS.getOrdinal()) {
                boolean unitChange = false;
                // Check to see if it is a unit change
                if (!weight.equals(this.getTotalWeight())) {
                    unitChange = BatchUtils.isUnitOnlyChanged(weight, this
                            .getTotalWeight());
                    this.getTotalWeight().deepCopy(weight);
                    if (!unitChange) {
                        lastUpdatedProductType = BatchModel.UPDATE_TYPE_TOTAL_WEIGHT;
                        // updateCalcFlags(weightAmount);
                        setModified(true);
                    }
                }
                if (!unitChange)
                    //this includes recalcTotalAmounts();
                    recalcAmounts();
            }
        } else {
            this.getTotalWeight().setValue("0");
        }
    }

    public void setTotalWeightAmountQuitly(AmountModel weight) {
        if (weight != null) {
            this.getTotalWeight().deepCopy(weight);
        } else {
            this.getTotalWeight().setValue("0");
        }
    }

    public void setTotalVolumeAmount(AmountModel volume) {
        if (volume != null /*&& volume.GetValueInStdUnitsAsDouble() != 0.0*/) {
            if (volume.getUnitType().getOrdinal() == UnitType.VOLUME
                    .getOrdinal()) {
                boolean unitChange = false;
                if (!this.getTotalVolume().equals(volume)) {
                    unitChange = BatchUtils.isUnitOnlyChanged(this
                            .getTotalVolume(), volume);
                    this.getTotalVolume().deepCopy(volume);
                    if (!unitChange) {
                        lastUpdatedProductType = UPDATE_TYPE_TOTAL_VOLUME;
                        // updateCalcFlags(volumeAmount);
                        setModified(true);
                    }
                }
                if (!unitChange)
                    //This will trigger recalcTotalAmounts() as well
                    recalcAmounts();
            }
        } else {
            this.getTotalVolume().setValue("0");
        }
    }

    public void setTotalVolumeAmountQuitly(AmountModel volume) {
        if (volume != null) {
            this.getTotalVolume().deepCopy(volume);
        } else {
            this.getTotalVolume().setValue("0");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProductBatchModel that = (ProductBatchModel) o;

        if (theoreticalYieldPercentAmount != null ? !theoreticalYieldPercentAmount.equals(that.theoreticalYieldPercentAmount) : that.theoreticalYieldPercentAmount != null)
            return false;
        if (theoreticalWeightAmount != null ? !theoreticalWeightAmount.equals(that.theoreticalWeightAmount) : that.theoreticalWeightAmount != null)
            return false;
        return theoreticalMoleAmount != null ? theoreticalMoleAmount.equals(that.theoreticalMoleAmount) : that.theoreticalMoleAmount == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (theoreticalYieldPercentAmount != null ? theoreticalYieldPercentAmount.hashCode() : 0);
        result = 31 * result + (theoreticalWeightAmount != null ? theoreticalWeightAmount.hashCode() : 0);
        result = 31 * result + (theoreticalMoleAmount != null ? theoreticalMoleAmount.hashCode() : 0);
        return result;
    }
}