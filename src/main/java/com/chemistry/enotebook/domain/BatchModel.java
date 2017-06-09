package com.chemistry.enotebook.domain;

import com.chemistry.enotebook.experiment.common.units.UnitType;
import com.chemistry.enotebook.experiment.datamodel.batch.BatchType;
import com.chemistry.enotebook.experiment.datamodel.common.Amount2;
import com.chemistry.enotebook.experiment.datamodel.common.SignificantFigures;
import com.chemistry.enotebook.experiment.utils.BatchUtils;
import com.chemistry.enotebook.experiment.utils.CeNNumberUtils;
import java.util.ArrayList;
import java.util.List;
import static com.epam.indigoeln.core.util.EqualsUtil.doubleEqZero;

public class BatchModel extends CeNAbstractModel implements Comparable<BatchModel>, StoicModelInterface {

    public static final long serialVersionUID = 7526472295622776147L;

    public static final int UPDATE_TYPE_MOLES = 0;
    public static final int UPDATE_TYPE_WEIGHT = 1;
    public static final int UPDATE_TYPE_VOLUME = 2;

    public static final int UPDATE_TYPE_TOTAL_WEIGHT = 3; // vb 2/2
    public static final int UPDATE_TYPE_TOTAL_VOLUME = 4;
    static final int UPDATE_TYPE_TOTAL_MOLARITY = 5;
    // Describes concentration of batch before updating with latests Amount
    private final AmountModel previousMolarAmount = new AmountModel(UnitType.MOLAR);
    boolean autoCalcOn = true;
    // limiting
    // reagent in the reaction
    boolean inCalculation = false; // Do not disturb. Batch is in process of calculating values
    // used to dynamically set the column names when used in a table
//    private String[] propertyNames;
    private ParentCompoundModel compound; // Holds structure formula wgt and other info.
    private AmountModel molecularWeightAmount = new AmountModel(UnitType.SCALAR); // Holds batch molecular weight
    private AmountModel moleAmount = new AmountModel(UnitType.MOLES); // Unitless amount indicating how much of an Avagadro's
    // number of molecules we have
    private AmountModel weightAmount = new AmountModel(UnitType.MASS); // AmountModel will contain unit conversions original amount
    // and original units
    private AmountModel loadingAmount = new AmountModel(UnitType.LOADING); // Loading is generally mmol/gram - tackles resins
    private AmountModel volumeAmount = new AmountModel(UnitType.VOLUME); // AmountModel in volume
    private AmountModel densityAmount = new AmountModel(UnitType.DENSITY); // Density of compound in g/mL
    private AmountModel molarAmount = new AmountModel(UnitType.MOLAR); // Describes concentration of batch
    private AmountModel purityAmount = new AmountModel(UnitType.SCALAR, 100); // % Purity info 100 - 0
    private AmountModel rxnEquivsAmount = new AmountModel(UnitType.SCALAR, 1.0, 1.0); // Represents equivalants of compound to a
    private SaltFormModel saltForm = new SaltFormModel("00"); // Must be from a vetted list
    /*
     * This will hold the List Key corresponding the Batch. It will be used while loading Batches to determine the listkey to which
     * the batch is assigned to, which helps in building the BatchesList object.
     */
    private AmountModel totalVolume = new AmountModel(UnitType.VOLUME);
    private AmountModel totalWeight = new AmountModel(UnitType.MASS);
    private AmountModel totalMolarity = new AmountModel(UnitType.MOLAR); // Total Amount made molarity
    private boolean limiting = false;
    private BatchType batchType = null;
    private double saltEquivs;
    private List<String> precursors = new ArrayList<>(); // holds compound ids that were used to create this batch.
    private int transactionOrder = 0;
    // uses constants from above
    private int lastUpdatedType = UPDATE_TYPE_MOLES;

    BatchModel() {
        this.compound = new ParentCompoundModel();
    }

    BatchModel(AmountModel molecularWeightAmount, AmountModel moleAmount, AmountModel weightAmount,
               AmountModel volumeAmount, AmountModel densityAmount, AmountModel molarAmount,
               AmountModel purityAmount, AmountModel rxnEquivsAmount, boolean limiting, BatchType batchType,
               AmountModel totalVolume, AmountModel totalWeight, AmountModel totalMolarity) {
        this.molecularWeightAmount = molecularWeightAmount;
        this.moleAmount = moleAmount;
        this.weightAmount = weightAmount;
        this.volumeAmount = volumeAmount;
        this.densityAmount = densityAmount;
        this.molarAmount = molarAmount;
        this.purityAmount = purityAmount;
        this.rxnEquivsAmount = rxnEquivsAmount;
        this.limiting = limiting;
        this.batchType = batchType;
        this.totalVolume = totalVolume;
        this.totalWeight = totalWeight;
        this.totalMolarity = totalMolarity;
    }

    public int getLastUpdatedType() {
        return lastUpdatedType;
    }

    public void setLastUpdatedType(int lastUpdatedType) {
        this.lastUpdatedType = lastUpdatedType;
    }

    private ParentCompoundModel getCompound() {
        return compound;
    }

    public AmountModel getDensityAmount() {
        return densityAmount;
    }

    public void setDensityAmount(AmountModel density) {
        if (density != null) {
            if (density.getUnitType().getOrdinal() == UnitType.DENSITY.getOrdinal()) {
                // Check to see if it is a unit change
                if (!densityAmount.equals(density)) {
                    densityAmount.deepCopy(density);
                    updateCalcFlags(densityAmount);
                    recalcAmounts();
                    this.modelChanged = true;
                }
            }
        } else {
            densityAmount.setValue("0");
        }
    }

    public void setDensityAmountQuitly(AmountModel density) {
        if (density != null) {
            if (density.getUnitType().getOrdinal() == UnitType.DENSITY.getOrdinal()) {
                densityAmount.deepCopy(density);
            }
        } else {
            densityAmount.setValue("0");
        }
    }

    public boolean isLimiting() {
        return limiting;
    }

    public void setLimiting(boolean limiting) {
        this.limiting = limiting;
        setModified(true);
    }

    public AmountModel getMolarAmount() {
        return molarAmount;
    }

    public void setMolarAmount(AmountModel molarAmnt) {
        if (molarAmnt != null) {
            if (molarAmnt.getUnitType().getOrdinal() == UnitType.MOLAR.getOrdinal()) {
                // Check to see if it is a unit change
                if (!molarAmount.equals(molarAmnt)) {
                    boolean unitChange = BatchUtils.isUnitOnlyChanged(molarAmount, molarAmnt);
                    molarAmount.deepCopy(molarAmnt);
                    if (!unitChange) {
                        recalcAmounts();
                        setModified(true);
                    }
                }
            }
        } else {
            molarAmount.setValue("0");
        }
    }

    public void setMolarAmountQuitly(AmountModel molarAmnt) {
        if (molarAmnt != null) {
            if (molarAmnt.getUnitType().getOrdinal() == UnitType.MOLAR.getOrdinal()) {
                molarAmount.deepCopy(molarAmnt);
            }
        } else {
            molarAmount.setValue("0");
        }
    }

    public AmountModel getMoleAmount() {
        return moleAmount;
    }

    public void setMoleAmount(AmountModel moles) {
        if (moles != null) {
            if (moles.getUnitType().getOrdinal() == UnitType.MOLES.getOrdinal()) {
                boolean unitChange = false;
                // Check to see if it is a unit change
                if (!moleAmount.equals(moles)) {
                    unitChange = BatchUtils.isUnitOnlyChanged(moleAmount, moles);
                    moleAmount.deepCopy(moles);
                    if (!unitChange) {
                        lastUpdatedType = UPDATE_TYPE_MOLES;
                        updateCalcFlags(moleAmount);
                        setModified(true);
                    }
                }
                if (!unitChange)
                    recalcAmounts();
            }
        } else {
            moleAmount.setValue("0");
        }
        setModelChanged(true);
    }

    public void setMoleAmountQuitly(AmountModel moles) {
        if (moles != null) {
            moleAmount.deepCopy(moles);
        } else {
            moleAmount.setValue("0");
        }
    }

    public AmountModel getMolecularWeightAmount() {
        double result = molecularWeightAmount.getValueInStdUnitsAsDouble();
        if (doubleEqZero(result)) {
            result = this.getMolWgtCalculated();
        }
        molecularWeightAmount.setValue(result);
        return molecularWeightAmount;
    }

    public void setMolecularWeightAmount(AmountModel molecularWeight) {
        if (molecularWeightAmount != null && !this.molecularWeightAmount.equals(molecularWeight)) {
            this.molecularWeightAmount.deepCopy(molecularWeight);
            setModified(true);
        }
    }

    public List<String> getPrecursors() {
        return precursors;
    }

    public void setPrecursors(List<String> precursors) {
        if (precursors == null)
            return;
        this.precursors = precursors;
        this.modelChanged = true;
    }

    public AmountModel getPurityAmount() {
        return purityAmount;
    }

    public void setPurityAmount(AmountModel purity) {
        if (purity != null) {
            if (!purityAmount.equals(purity)) {
                purityAmount.deepCopy(purity);
                recalcAmounts();
                setModified(true);
            }
        } else {
            purityAmount.setValue("100", true);
        }
        updateCalcFlags(purityAmount);
    }

    public AmountModel getRxnEquivsAmount() {
        return rxnEquivsAmount;
    }

    public void setRxnEquivsAmount(AmountModel equiv) {
        if (!rxnEquivsAmount.equals(equiv)) {
            rxnEquivsAmount.deepCopy(equiv);
            updateCalcFlags(rxnEquivsAmount);
            setModified(true);
        }
    }

    public void setRxnEquivsAmountQuitly(AmountModel equiv) {
        if (!rxnEquivsAmount.equals(equiv)) {
            rxnEquivsAmount.deepCopy(equiv);
        }
    }

    private double getSaltEquivs() {
        return saltEquivs;
    }

    private SaltFormModel getSaltForm() {
        return saltForm;
    }

    public AmountModel getVolumeAmount() {
        return volumeAmount;
    }

    public void setVolumeAmount(AmountModel volume) {
        if (volume != null) {
            if (volume.getUnitType().getOrdinal() == UnitType.VOLUME.getOrdinal()) {
                boolean unitChange = false;
                if (!volumeAmount.equals(volume)) {
                    unitChange = BatchUtils.isUnitOnlyChanged(volumeAmount, volume);
                    volumeAmount.deepCopy(volume);
                    if (!unitChange) {
                        lastUpdatedType = UPDATE_TYPE_VOLUME;
                        updateCalcFlags(volumeAmount);
                        setModified(true);
                    }
                }
                if (!unitChange)
                    recalcAmounts();
            }
        } else {
            this.volumeAmount.setValue("0");
        }
    }

    public void setVolumeAmountQuitly(AmountModel volume) {
        if (volume != null) {
            volumeAmount.deepCopy(volume);
        } else {
            this.volumeAmount.setValue("0");
        }
    }

    public AmountModel getWeightAmount() {
        return weightAmount;
    }

    public void setWeightAmount(AmountModel weight) {
        if (weight != null) {
            if (weight.getUnitType().getOrdinal() == UnitType.MASS.getOrdinal()) {
                boolean unitChange = false;
                // Check to see if it is a unit change
                if (!weightAmount.equals(weight)) {
                    unitChange = BatchUtils.isUnitOnlyChanged(weightAmount, weight);
                    weightAmount.deepCopy(weight);
                    if (!unitChange) {
                        lastUpdatedType = UPDATE_TYPE_WEIGHT;
                        updateCalcFlags(weightAmount);
                        setModified(true);
                    }
                }
                if (!unitChange)
                    recalcAmounts();
            }
        } else {
            weightAmount.setValue("0");
        }
    }

    public void setWeightAmountQuitly(AmountModel weight) {
        if (weight != null) {
            weightAmount.deepCopy(weight);
        } else {
            weightAmount.setValue("0");
        }
    }

    public boolean isAutoCalcOn() {
        return autoCalcOn;
    }

    int getTransactionOrder() {
        return transactionOrder;
    }

    void setTransactionOrder(int transactionOrder) {
        this.transactionOrder = transactionOrder;
        this.modelChanged = true;
    }

    public BatchType getBatchType() {
        return batchType;
    }

    public void setBatchType(BatchType batchType) {
        if (batchType == null)
            return;
        this.batchType = batchType;
        this.modelChanged = true;
    }

    public void setPurityAmountQuitly(AmountModel purity) {
        if (purity != null) {
            purityAmount.deepCopy(purity);
        } else {
            purityAmount.setValue("100", true);
        }
    }

    public void setPurity(double value) {
        if (value > 0.0 && value <= 100.0) {
            purityAmount.setValue(value, false);
        } else {
            purityAmount.setValue("100", true);
        }
        updateCalcFlags(purityAmount);
    }

    public String getStoicReactionRole() {
        if (this.getBatchType() != null)
            return this.getBatchType().toString();
        else
            return "";
    }

    // For TotalVolume
    public AmountModel getTotalVolume() {
        return this.totalVolume;
    }

    public AmountModel getTotalWeight() {
        return totalWeight;
    }

    private void updateCalcFlags(Amount2 changingAmount) {
        if (!changingAmount.isCalculated()) {
            if (changingAmount.equals(rxnEquivsAmount)) {
                moleAmount.setCalculated(true);
                if (!this.isLimiting())
                    weightAmount.setCalculated(true);
                if (!volumeAmount.isCalculated() && isVolumeConnectedToMass())
                    volumeAmount.setCalculated(true);
            } else if (changingAmount.equals(weightAmount) || changingAmount.equals(moleAmount)) {
                rxnEquivsAmount.setCalculated(true);
                if (!volumeAmount.isCalculated() && isVolumeConnectedToMass())
                    volumeAmount.setCalculated(true);
            } else if (changingAmount.equals(volumeAmount) && isVolumeConnectedToMass()) {
                rxnEquivsAmount.setCalculated(true);
                weightAmount.setCalculated(true);
                moleAmount.setCalculated(true);
            }
            // applyLatestSigDigits(getSmallestSigFigs());
        }
    }

    private void updateWeightFromMoles() {
        weightAmount.setCalculated(true);
        List<AmountModel> amts = new ArrayList<>();
        // weight = weight/mole * moles
        // In this case moles are always mMoles and the default return of mole amount is in mmoles
        // Hence when we setValue for weight we are doing so in mg and no conversion is necessary.
        double result = getMolWgt() * getMoleAmount().getValueInStdUnitsAsDouble();
        if (loadingAmount.doubleValue() > 0.0) {
            // calc by loadingAmount mmol/g - doesn't use molWgt.
            // mg = 1000 mg/g * mmol/(mmol/g)
            result = (1000 * getMoleAmount().getValueInStdUnitsAsDouble() / loadingAmount.getValueInStdUnitsAsDouble());
            amts.add(loadingAmount);
            amts.add(moleAmount);
        } else if (getPurityAmount().doubleValue() < 100d && getPurityAmount().doubleValue() > 0.0) {
            result = result / (getPurityAmount().doubleValue() / 100);
            amts.add(moleAmount);
            amts.add(purityAmount);
            amts.add(molecularWeightAmount);
        } else {
            amts.add(moleAmount);
            amts.add(molecularWeightAmount);
        }
        // Applies SignificantFigures to weightAmount
        applySigFigRules(weightAmount, amts);
        amts.clear();

        // We just got the result for mg * purity),
        // Now we need to make sure the answer makes sense for the units of weight currently being used.
        weightAmount.setValueInStdUnits(result, true);
    }

    private void updateMolesFromWeight() {
        double result = 0d;
        List<AmountModel> amts = new ArrayList<>();
        // weight std unit = mg; density std unit = mg/mmol
        // Weight = 9.82m/s^2 * mass
        if (loadingAmount.doubleValue() > 0.0) {
            // mmoles = (mmol/g) * (g/1000mg) * (weightAmount in std units = mg)
            result = loadingAmount.getValueInStdUnitsAsDouble() * (weightAmount.getValueInStdUnitsAsDouble() / 1000);
            amts.add(loadingAmount);
            amts.add(weightAmount);
        } else if (getMolWgt() > 0.0) {
            // TODO: Warning. Using getMolWgt() in the future may not return
            // standard units. Make sure this changes if the
            // users ever get the opportunity to change units of
            // MolWt.
            result = weightAmount.getValueInStdUnitsAsDouble() / getMolWgt();
            amts.add(weightAmount);
            amts.add(molecularWeightAmount);
            // we just got the result for mg/mmole,
            // now we need to make sure the answer makes sense for the units of
            // weight currently being used.
            if (getPurityAmount().doubleValue() < 100d && purityAmount.doubleValue() > 0.0) {
                result = result * (purityAmount.doubleValue() / 100);
                amts.add(purityAmount);
            }
        }
        applySigFigRules(moleAmount, amts);
        amts.clear();
        moleAmount.setValueInStdUnits(result, true);
    }

    public void recalcAmounts() {
        // Make sure we don't get into a loop!
        // And that we don't lose information on load.
        if (!autoCalcOn || inCalculation || isBeingCloned())
            return;

        //if This batch is a SOLVENT, then do not recalculate its amounts.
        if (getBatchType() == null || getBatchType().equals(BatchType.SOLVENT))
            return;

        List<AmountModel> amts = new ArrayList<>();
        inCalculation = true;
        // Check which value was set by hand: solid or liquid
        // Molar type as last updated not considered here because moles is considered driver
        // when there is a tie in flags.
        if (lastUpdatedType == UPDATE_TYPE_VOLUME && !volumeAmount.isCalculated()) {
            // We need to update moles and weight from volume
            // Molarity takes precedence over density
            if (molarAmount.doubleValue() > 0) {
                amts.add(molarAmount);
                amts.add(volumeAmount);
                applySigFigRules(moleAmount, amts);
                amts.clear(); // important to clear the amts list
                // Update mole amount
                // Std unit for molar is mMolar
                //
                // mMoles = (mole/L) * mL
                moleAmount.setValueInStdUnits(molarAmount.getValueInStdUnitsAsDouble() * volumeAmount.getValueInStdUnitsAsDouble(),
                        true);
                updateWeightFromMoles();
            } else if (densityAmount.doubleValue() > 0) {
                // find governing sig figs
                amts.add(volumeAmount);
                amts.add(densityAmount);
                applySigFigRules(weightAmount, amts);
                amts.clear();// important to clear the amts list
                // mg = (mL * g/mL)/ (1000 mg/g)
                weightAmount.setValueInStdUnits(1000 * volumeAmount.getValueInStdUnitsAsDouble()
                        * densityAmount.getValueInStdUnitsAsDouble(), true);
                updateMolesFromWeight();
            }
            // SourceForge 3.4 bug 28030 - remove data not connected
            if (!isVolumeConnectedToMass()) {
                // set weight, moles and rxn equivs to default values.
                weightAmount.reset();
                moleAmount.reset();
                rxnEquivsAmount.reset();
            }
        } else {
            if ((lastUpdatedType != UPDATE_TYPE_WEIGHT && !moleAmount.isCalculated()) || !rxnEquivsAmount.isCalculated()
                    || moleAmount.doubleValue() > 0 && lastUpdatedType != UPDATE_TYPE_WEIGHT) {
                updateWeightFromMoles();
            } else {
                updateMolesFromWeight();
            }
            // Now that the solids are straightened out, we can calc the liquid
            if (molarAmount.doubleValue() > 0) {
                // Due to artf60208 :Singleton: correct sig fig processing for Molarity in Stoich Table (CEN-705) comment sig fig calculation
                //				// update volume
                //				amts.add(moleAmount);
                //				amts.add(molarAmount);
                //				if (volumeAmount.isCalculated() && weightAmount.isCalculated())
                //					volumeAmount.setSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
                //				else
                //					applySigFigRules(volumeAmount, amts);

                //				amts.clear();// important to clear the amts list
                volumeAmount.setSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
                volumeAmount.setValueInStdUnits(moleAmount.getValueInStdUnitsAsDouble() / molarAmount.getValueInStdUnitsAsDouble(),
                        true);
            } else if (densityAmount.doubleValue() > 0) {
                // Due to artf60208 :Singleton: correct sig fig processing for Molarity in Stoich Table (CEN-705) comment sig fig calculation
                //				amts.add(weightAmount);
                //				amts.add(densityAmount);
                //				applySigFigRules(volumeAmount, amts);
                //				amts.clear();// important to clear the amts list

                volumeAmount.setSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
                // update volume from weight value: mg /(1000mg/g)* (g/mL) = mL
                volumeAmount.setValueInStdUnits(weightAmount.getValueInStdUnitsAsDouble()
                        / (1000 * densityAmount.getValueInStdUnitsAsDouble()), true);
            }
            if (!isVolumeConnectedToMass()) {
                // set weight, moles and rxn equivs to default values.
                volumeAmount.softReset();
            }
        }
        inCalculation = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BatchModel that = (BatchModel) o;

        if (autoCalcOn != that.autoCalcOn) return false;
        if (inCalculation != that.inCalculation) return false;
        if (limiting != that.limiting) return false;
        if (Double.compare(that.saltEquivs, saltEquivs) != 0) return false;
        if (transactionOrder != that.transactionOrder) return false;
        if (lastUpdatedType != that.lastUpdatedType) return false;
        if (previousMolarAmount != null ? !previousMolarAmount.equals(that.previousMolarAmount) : that.previousMolarAmount != null)
            return false;
        if (compound != null ? !compound.equals(that.compound) : that.compound != null) return false;
        if (molecularWeightAmount != null ? !molecularWeightAmount.equals(that.molecularWeightAmount) : that.molecularWeightAmount != null)
            return false;
        if (moleAmount != null ? !moleAmount.equals(that.moleAmount) : that.moleAmount != null) return false;
        if (weightAmount != null ? !weightAmount.equals(that.weightAmount) : that.weightAmount != null) return false;
        if (loadingAmount != null ? !loadingAmount.equals(that.loadingAmount) : that.loadingAmount != null)
            return false;
        if (volumeAmount != null ? !volumeAmount.equals(that.volumeAmount) : that.volumeAmount != null) return false;
        if (densityAmount != null ? !densityAmount.equals(that.densityAmount) : that.densityAmount != null)
            return false;
        if (molarAmount != null ? !molarAmount.equals(that.molarAmount) : that.molarAmount != null) return false;
        if (purityAmount != null ? !purityAmount.equals(that.purityAmount) : that.purityAmount != null) return false;
        if (rxnEquivsAmount != null ? !rxnEquivsAmount.equals(that.rxnEquivsAmount) : that.rxnEquivsAmount != null)
            return false;
        if (saltForm != null ? !saltForm.equals(that.saltForm) : that.saltForm != null) return false;
        if (totalVolume != null ? !totalVolume.equals(that.totalVolume) : that.totalVolume != null) return false;
        if (totalWeight != null ? !totalWeight.equals(that.totalWeight) : that.totalWeight != null) return false;
        if (totalMolarity != null ? !totalMolarity.equals(that.totalMolarity) : that.totalMolarity != null)
            return false;
        if (batchType != null ? !batchType.equals(that.batchType) : that.batchType != null) return false;
        return precursors != null ? precursors.equals(that.precursors) : that.precursors == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = previousMolarAmount != null ? previousMolarAmount.hashCode() : 0;
        result = 31 * result + (compound != null ? compound.hashCode() : 0);
        result = 31 * result + (molecularWeightAmount != null ? molecularWeightAmount.hashCode() : 0);
        result = 31 * result + (moleAmount != null ? moleAmount.hashCode() : 0);
        result = 31 * result + (weightAmount != null ? weightAmount.hashCode() : 0);
        result = 31 * result + (loadingAmount != null ? loadingAmount.hashCode() : 0);
        result = 31 * result + (volumeAmount != null ? volumeAmount.hashCode() : 0);
        result = 31 * result + (densityAmount != null ? densityAmount.hashCode() : 0);
        result = 31 * result + (molarAmount != null ? molarAmount.hashCode() : 0);
        result = 31 * result + (purityAmount != null ? purityAmount.hashCode() : 0);
        result = 31 * result + (rxnEquivsAmount != null ? rxnEquivsAmount.hashCode() : 0);
        result = 31 * result + (saltForm != null ? saltForm.hashCode() : 0);
        result = 31 * result + (totalVolume != null ? totalVolume.hashCode() : 0);
        result = 31 * result + (totalWeight != null ? totalWeight.hashCode() : 0);
        result = 31 * result + (totalMolarity != null ? totalMolarity.hashCode() : 0);
        result = 31 * result + (autoCalcOn ? 1 : 0);
        result = 31 * result + (inCalculation ? 1 : 0);
        result = 31 * result + (limiting ? 1 : 0);
        result = 31 * result + (batchType != null ? batchType.hashCode() : 0);
        temp = Double.doubleToLongBits(saltEquivs);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (precursors != null ? precursors.hashCode() : 0);
        result = 31 * result + transactionOrder;
        result = 31 * result + lastUpdatedType;
        return result;
    }

    // Before playing here familiarize yourself with the
    // layout rules for stoichiometry.
    public int compareTo(BatchModel ab) {
        int result = 0;
        if (ab != null) {
            result = this.getBatchType().compareTo(ab.getBatchType());
            // Precedence should be batchNumber if Product otherwise (Transaction Step Number) for now: Compound Number then batch
            // then formula
            if (result == 0) {
                if (getCompound() != null)
                    if (ab.getCompound() != null)
                        result = (getCompound().compareTo(ab.getCompound()));
                    else
                        result = 1;
            }
        }
        return result;
    }

    private boolean isVolumeConnectedToMass() {
        return (densityAmount.doubleValue() > 0 || molarAmount.doubleValue() > 0);
    }

    /**
     * Amounts that are all interrelated regarding batch calculations. SigDigits apply.
     */
    protected List<AmountModel> getCalculatedAmounts() {
        List<AmountModel> result = new ArrayList<>();
        result.add(getMoleAmount());
        result.add(getWeightAmount());
        result.add(getVolumeAmount());
        result.add(getRxnEquivsAmount()); // Use this to set initial sigdigits = 3 by setting to 1.00 as default.
        return result;
    }

    public boolean shouldApplySigFigRules() {
        return (!weightAmount.isCalculated() || !volumeAmount.isCalculated());
    }

    /**
     *
     */
    public boolean shouldApplyDefaultSigFigs() {
        return (!this.rxnEquivsAmount.isCalculated() || !this.moleAmount.isCalculated() || !this.molarAmount.isCalculated());
    }

    /**
     * For all calculated amounts find those that are calculated and set default number of significant digits to the value passed.
     */
    public void applyLatestSigDigits(int defaultSigs) {
        List<AmountModel> amts = getCalculatedAmounts();
        amts.stream().filter(Amount2::isCalculated).forEach(amt -> amt.setSigDigits(defaultSigs));
    }

    /**
     * Note: Avoid Side-effects.  If your intention is to trigger a new value for MolecularWeight, please set it explicitly.
     * Prefer accessing the actual AmountModel object.doubleValue() to calling this method unless your intention is to recalculate.
     *
     * @return Returns molecularWeightAmount.doubleValue() or the calculated amount if the double Value = the defaultValue of Zero
     */
    public double getMolWgt() {
        double result = molecularWeightAmount.doubleValue();
        // return 0 if no compound weight has been set -or- return weight set by user
        if (molecularWeightAmount.isValueDefault() && molecularWeightAmount.isCalculated()) {
            result = getMolWgtCalculated();
        }
        return result;
    }

    /**
     * @return weight based on molecularWeight of the compound + (Number of Salt Equivalents * Salt MW)
     */
    private double getMolWgtCalculated() {
        // Significant Figures for calculated molecular weight was deemed to be ignored.
        // Scientists want MW calculated out to the third place behind the decimal or 3 fixed figures if they are available.
        // Do not enforce sig figs here.
        // Don't return more than three places after decimal point
        double result = 0.0;

        // uncomment if there is a case when compound.getMolWgt() > 0.0 is true
//        if (compound.getMolWgt() > 0.0) {
//            result = compound.getMolWgt() + (getSaltForm().getMolWgt() * getSaltEquivs());
//            String test = Double.toString(result);
//            if (test.indexOf(".") > 0)
//                if (test.substring(test.indexOf(".") + 1, test.length()).length() > 3)
//                    result = new BigDecimal(result).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
//        }
        return result;
    }

    // All stoic interface methods. these methods are delegated to internal getter/setters

    private void setMolWgtCalculated(double value) {
        SignificantFigures sigs = new SignificantFigures(value);
        // Only set if sigfigs are greater than zero.
        if (sigs.getNumberSignificantFigures() > 0)
            molecularWeightAmount.setSigDigits(sigs.getNumberSignificantFigures());
        molecularWeightAmount.setValue(sigs.doubleValue());
    }

    public AmountModel getStoicMoleAmount() {
        return this.getMoleAmount();
    }

    public void setStoicMoleAmount(AmountModel molAmt) {
        this.setMoleAmount(molAmt);
    }

    public AmountModel getStoicRxnEquivsAmount() {
        return this.getRxnEquivsAmount();
    }

    public void setStoicRxnEquivsAmount(AmountModel rxnEquivAmt) {
        this.setRxnEquivsAmount(rxnEquivAmt);
    }

    public boolean isStoicLimiting() {
        return this.isLimiting();
    }

    public void setStoicLimiting(boolean isLimiting) {
        this.setLimiting(isLimiting);
    }

    public AmountModel getStoicMolarAmount() {
        return this.getMolarAmount();
    }

    public void setStoicMolarAmount(AmountModel molarAmt) {
        this.setMolarAmount(molarAmt);
    }

    public AmountModel getStoicMolecularWeightAmount() {
        return this.getMolecularWeightAmount();
    }

    public AmountModel getStoicWeightAmount() {
        return this.getWeightAmount();
    }

    public AmountModel getStoicDensityAmount() {
        return this.getDensityAmount();
    }

    public AmountModel getStoicVolumeAmount() {
        return this.getVolumeAmount();
    }

    public void setStoicVolumeAmount(AmountModel volume) {
        this.setVolumeAmount(volume);
    }

    public int getStoicTransactionOrder() {
        return this.getTransactionOrder();
    }

    public void setStoicTransactionOrder(int transactionOrder) {
        this.setTransactionOrder(transactionOrder);

    }

    public AmountModel getStoicPurityAmount() {
        return this.getPurityAmount();
    }

    public AmountModel getPreviousMolarAmount() {
        return previousMolarAmount;
    }

    public void setPreviousMolarAmount(AmountModel vpreMolarAmount) {
        if (vpreMolarAmount != null) {
            if (vpreMolarAmount.getUnitType().getOrdinal() == UnitType.MOLAR.getOrdinal()) {
                boolean unitChange;
                // Check to see if it is a unit change
                if (!previousMolarAmount.equals(vpreMolarAmount)) {
                    unitChange = BatchUtils.isUnitOnlyChanged(previousMolarAmount, vpreMolarAmount);
                    previousMolarAmount.deepCopy(vpreMolarAmount);
                    if (!unitChange) {
                        setModified(true);
                    }
                }
            }
        } else {
            previousMolarAmount.setValue("0");
        }
    }

    public void applySigFigRules(AmountModel amt, List<AmountModel> amts) {
        if (shouldApplySigFigRules()) {
            amt.setSigDigits(CeNNumberUtils.getSmallestSigFigsFromAmountModelList(amts));
        } else {
            if (shouldApplyDefaultSigFigs())
                amt.setSigDigits(CeNNumberUtils.DEFAULT_SIG_DIGITS);
        }
    }

    // empty Impl. ProdBAtchModel will override with correct impl
    public AmountModel getTheoreticalWeightAmount() {
        return null;
    }

    public void setTheoreticalWeightAmount(AmountModel theoreticalWeightAmount) {

    }

    public AmountModel getTheoreticalMoleAmount() {
        return null;
    }

    public void setTheoreticalMoleAmount(AmountModel theoreticalMoleAmount) {

    }

    public AmountModel getTotalMolarity() {
        return totalMolarity;
    }

    public void resetAmount(String amountName) {
        if (amountName.equals(CeNConstants.MOLE_AMOUNT)) {
            this.getMoleAmount().reset();
        }
    }

}

