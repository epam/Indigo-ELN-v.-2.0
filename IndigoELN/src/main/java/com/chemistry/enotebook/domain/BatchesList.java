package com.chemistry.enotebook.domain;

import com.chemistry.enotebook.experiment.common.units.UnitType;
import com.chemistry.enotebook.experiment.datamodel.batch.BatchType;
import com.chemistry.enotebook.experiment.datamodel.common.Amount2;
import com.chemistry.enotebook.experiment.utils.CeNNumberUtils;

import java.util.ArrayList;
import java.util.List;

public class BatchesList<E extends BatchModel> extends CeNAbstractModel implements Comparable<BatchesList<E>>, StoicModelInterface {

    private static final long serialVersionUID = -229096776631434914L;
    // These attributes are used for List type ( Models > 1) in Stoich
    private final AmountModel listMolecularWeightAmount = new AmountModel(UnitType.SCALAR); // Holds batch molecular weight
    private final AmountModel listMoleAmount = new AmountModel(UnitType.MOLES); // Unitless amount indicating how much of an Avagadro's
    private final AmountModel listWeightAmount = new AmountModel(UnitType.MASS); // AmountModel will contain unit conversions original amount
    private final AmountModel listVolumeAmount = new AmountModel(UnitType.VOLUME); // AmountModel in volume
    private final AmountModel listDensityAmount = new AmountModel(UnitType.DENSITY); // Density of compound in g/mL
    private final AmountModel listMolarAmount = new AmountModel(UnitType.MOLAR); // Describes concentration of batch
    private final AmountModel listPurityAmount = new AmountModel(UnitType.SCALAR, 100); // % Purity info 100 - 0
    private final AmountModel listRxnEquivsAmount = new AmountModel(UnitType.SCALAR, 0, 0); //
    private final AmountModel listPreviousMolarAmount = new AmountModel(UnitType.MOLAR);
    // ArrayList has ProductBatch or MonomerBatch models in the list
    private final List<E> batchModels = new ArrayList<>();
    // Required for StoicModel
    // Position this list of batchMode
    // A,B,C for monomers and P1,P2 for final products,intermediate products
    private String position = "";
    //these are the flags to handle if all the BatchModels in the list has same value or  user has entered a specific value to any single BatchModel
    private boolean isAllBatchModelsMoleAmountIsSame = true;
    private boolean isAllBatchModelsWeightAmountIsSame = true;
    private boolean isAllBatchModelsVolumeAmountIsSame = true;
    private boolean isAllBatchModelsDensityAmountIsSame = true;
    private boolean isAllBatchModelsMolarAmountIsSame = true;
    private boolean isAllBatchModelsPurityAmountIsSame = true;
    private boolean isAllBatchModelsRxnEquivsIsSame = true;

    public BatchesList() {
    }

    public void addBatch(E batch) {
        boolean found = false;

        for (E batchModel : batchModels) {
            if (batchModel != null && batchModel.equals(batch)) {
                found = true;
                break;
            }
        }

        if (!found) {
            batchModels.add(batch);
        }

        setModelChanged(true);
    }

    public List<E> getBatchModels() {
        return batchModels;
    }

    String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        if (!this.position.equals(position)) {
            this.position = position;
            this.modelChanged = true;
        }
    }

    public int compareTo(BatchesList<E> anotherBatchesList) throws ClassCastException {
//		if (!(anotherBatchesList instanceof BatchesList))
//			throw new ClassCastException("A BatchesList object expected.");
        int thisPos = 0;
        int otherPos = 0;
        try {
            String anotherListPos = anotherBatchesList.getPosition();
            char cother = anotherListPos.charAt(0);
            otherPos = Character.getNumericValue(cother);
            char cthis = this.position.charAt(0);
            thisPos = Character.getNumericValue(cthis);

        } catch (Exception e) {
            // Any exception just ignore and return 0
        }
        // Compares this object with the specified object for order.
        // Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
        // specified object.
        return thisPos - otherPos;
    }

    /*
     * As Mole Amount is same for the all the Batches in the list, Returns Molar amount for the first Batch in the list.
     *
     * This is a StoicModelInterface method
     */
    public AmountModel getStoicMoleAmount() {
        if (getListSize() == 1 || (this.isAllBatchModelsMoleAmountIsSame)) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getStoicMoleAmount();
        } else {
            return listMoleAmount;
        }
    }

    /*
     * Sets Mole amount to all the Batches in the list
     *
     * This is a StoicModelInterface method
     */
    public void setStoicMoleAmount(AmountModel molAmt) {
        if (!this.getStoicMoleAmount().equals(molAmt)) {
            for (E batchModel1 : batchModels) {
                batchModel1.setMoleAmount(molAmt);
            }
            this.modelChanged = true;
            this.updateAllListLevelFlags();
        }
    }

    /*
     * Returns ReactionRole
     *
     * This is a StoicModelInterface method
     */
    public String getStoicReactionRole() {
        return CeNConstants.BATCH_TYPE_REACTANT;
    }

    /*
     * As Molar Amount is same for the all the Batches in the list, method returns Molar amount for the first
     *
     * This is a StoicModelInterface method
     */
    public AmountModel getStoicRxnEquivsAmount() {
        if (getListSize() == 1 || (this.isAllBatchModelsRxnEquivsIsSame)) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getRxnEquivsAmount();
        } else {
            this.listRxnEquivsAmount.setValue("0");
            this.listRxnEquivsAmount.setDefaultValue("0");
            return listRxnEquivsAmount;
        }

    }

    /*
     * Sets rxnEquivAmt to all the batches in the list. This is a StoicModelInterface method
     */
    public void setStoicRxnEquivsAmount(AmountModel rxnEquivAmt) {
        if (!this.getStoicRxnEquivsAmount().equals(rxnEquivAmt)) {
            for (E batchModel1 : batchModels) {
                batchModel1.setRxnEquivsAmount(rxnEquivAmt);
            }
            setModelChanged(true);
            this.updateAllListLevelFlags();
        }
    }

    /*
     * As Limiting property is same for the all the Batches in the list, method returns Limiting property for the first
     *
     * This is a StoicModelInterface method
     */
    public boolean isStoicLimiting() {
        BatchModel batchModel = batchModels.get(0);
        return batchModel.isLimiting();
    }

    /*
     * Sets limiting property to all the Batches in the list
     *
     * This is a StoicModelInterface method
     */
    public void setStoicLimiting(boolean limiting) {
        if (this.isStoicLimiting() != limiting) {
            for (E batchModel1 : batchModels) {
                batchModel1.setLimiting(limiting);
            }
            this.modelChanged = true;
        }
    }

    // For MolecularWeight
    public AmountModel getStoicMolecularWeightAmount() {
        if (getListSize() == 1) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getStoicMolecularWeightAmount();
        } else {
            return listMolecularWeightAmount;
        }
    }

    // For Weight
    public AmountModel getStoicWeightAmount() {
        if (getListSize() == 1 || (this.isAllBatchModelsWeightAmountIsSame)) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getStoicWeightAmount();
        } else {
            return listWeightAmount;
        }
    }

    // For Density
    public AmountModel getStoicDensityAmount() {
        if (getListSize() == 1 || (this.isAllBatchModelsDensityAmountIsSame)) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getDensityAmount();
        } else {
            return listDensityAmount;
        }

    }

    // For Volume
    public AmountModel getStoicVolumeAmount() {
        if (getListSize() == 1 || (this.isAllBatchModelsVolumeAmountIsSame)) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getStoicVolumeAmount();
        } else {
            return listVolumeAmount;
        }

    }

    public void setStoicVolumeAmount(AmountModel volume) {
        for (E batchModel1 : batchModels) {
            batchModel1.setVolumeAmount(volume);
        }
        setModelChanged(true);
        //Set the same value at list level. ( value and isCalc )
        this.updateAllListLevelFlags();
    }

    // For TotalWeight
    public AmountModel getTotalWeight() {
        if (getListSize() == 1) {
            return null;
        } else {
            return null;
        }
    }

    // For TotalVolume
    public AmountModel getTotalVolume() {
        if (getListSize() == 1) {
            return null;
        } else {
            return null;
        }
    }

    public int getStoicTransactionOrder() {
        int size = this.batchModels.size();
        if (size > 0) {
            BatchModel model = this.batchModels.get(0);
            return model.getTransactionOrder();
        } else
            return 0;
    }

    public void setStoicTransactionOrder(int transactionOrder) {
        for (E batchModel : this.batchModels) {
            batchModel.setTransactionOrder(transactionOrder);
        }
        this.modelChanged = true;
    }

    // For Purity amount
    public AmountModel getStoicPurityAmount() {
        if (getListSize() == 1 || (this.isAllBatchModelsPurityAmountIsSame)) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getStoicPurityAmount();
        } else {
            return listPurityAmount;
        }
    }

    public AmountModel getStoicMolarAmount() {
        if (getListSize() == 1 || (this.isAllBatchModelsMolarAmountIsSame)) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getStoicMolarAmount();
        } else {
            return listMolarAmount;
        }
    }

    public void setStoicMolarAmount(AmountModel molarityModel) {
        for (E batchModel1 : batchModels) {
            batchModel1.setMolarAmount(molarityModel);
        }
        setModelChanged(true);
        this.updateAllListLevelFlags();

    }

    private int getListSize() {
        return batchModels.size();
    }

    public boolean isAutoCalcOn() {
        if (getListSize() >= 1) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.isAutoCalcOn();
        } else
            return true;
    }

    public boolean shouldApplySigFigRules() {

        return (!getStoicWeightAmount().isCalculated() || !getStoicVolumeAmount().isCalculated());

    }

    public boolean shouldApplyDefaultSigFigs() {
        return (!getStoicRxnEquivsAmount().isCalculated() || !getStoicMoleAmount().isCalculated() || !getStoicMolarAmount().isCalculated());

    }

    public void applyLatestSigDigits(int defaultSigs) {
        List<AmountModel> amts = getCalculatedAmounts();
        amts.stream().filter(Amount2::isCalculated).forEach(amt -> {
            amt.setSigDigits(defaultSigs);
        });
    }

    /**
     * Amounts that are all interrelated regarding batch calculations. SigDigits apply.
     */
    private List<AmountModel> getCalculatedAmounts() {
        ArrayList<AmountModel> result = new ArrayList<>();
        result.add(getStoicMoleAmount());
        result.add(getStoicWeightAmount());
        result.add(getStoicVolumeAmount());
        result.add(getStoicRxnEquivsAmount()); // Use this to set initial sigdigits = 3 by setting to 1.00 as default.
        return result;
    }

    public AmountModel getPreviousMolarAmount() {
        if (getListSize() >= 1) {
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getPreviousMolarAmount();
        } else {
            return listPreviousMolarAmount;
        }
    }

    public void setPreviousMolarAmount(AmountModel molarAmount) {
        if (getListSize() >= 1) {
            BatchModel batchModel = batchModels.get(0);
            batchModel.setPreviousMolarAmount(molarAmount);
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

    public BatchType getBatchType() {
        if (getListSize() >= 1) {
            //this is not true in case of stoicBatchesList Since this list may contain SOLV.REAG,MIXTURES etc
            BatchModel batchModel = batchModels.get(0);
            return batchModel.getBatchType();
        } else {
            return null;
        }
    }

    //	empty Impl. ProdBAtchModel will ovveride with correct impl
    public AmountModel getTheoreticalWeightAmount() {
        if (getBatchType().getOrdinal() == BatchType.ACTUAL_PRODUCT_ORDINAL) {
            if (getListSize() >= 1) {
                ProductBatchModel batchModel = (ProductBatchModel) batchModels.get(0);
                return batchModel.getTheoreticalWeightAmount();
            }
        }
        return null;
    }

    public void recalcAmounts() {
        batchModels.forEach(BatchModel::recalcAmounts);
    }

    public AmountModel getTheoreticalMoleAmount() {
        if (getBatchType().getOrdinal() == BatchType.ACTUAL_PRODUCT_ORDINAL) {
            if (getListSize() >= 1) {

                ProductBatchModel batchModel = (ProductBatchModel) batchModels.get(0);
                return batchModel.getTheoreticalMoleAmount();
            }
        }
        return null;
    }

    public void setTheoreticalMoleAmount(AmountModel theoreticalMoleAmount) {
        if (getBatchType().getOrdinal() == BatchType.ACTUAL_PRODUCT_ORDINAL) {
            for (E batchModel1 : batchModels) {
                ProductBatchModel batchModel = (ProductBatchModel) batchModel1;
                batchModel.setTheoreticalMoleAmount(theoreticalMoleAmount);
            }
        }
    }

    private boolean findIfAllMoleAmountsAreSameInTheList() {
        if (this.batchModels.size() < 2) {
            return true;
        } else {
            int size = this.batchModels.size();
            BatchModel refBatchModel = this.batchModels.get(0);
            for (int i = 1; i < size; i++) {
                BatchModel batchModel = this.batchModels.get(i);
                if (!refBatchModel.getMoleAmount().equalsByValueAndUnits(batchModel.getMoleAmount())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findIfAllMolarAmountsAreSameInTheList() {
        if (this.batchModels.size() < 2) {
            return true;
        } else {
            int size = this.batchModels.size();
            BatchModel refBatchModel = this.batchModels.get(0);
            for (int i = 1; i < size; i++) {
                BatchModel batchModel = this.batchModels.get(i);
                if (!refBatchModel.getMolarAmount().equalsByValueAndUnits(batchModel.getMolarAmount())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findIfAllRxnEquivsAmountsAreSameInTheList() {
        if (this.batchModels.size() < 2) {
            return true;
        } else {
            int size = this.batchModels.size();
            BatchModel refBatchModel = this.batchModels.get(0);
            for (int i = 1; i < size; i++) {
                BatchModel batchModel = this.batchModels.get(i);
                if (!refBatchModel.getRxnEquivsAmount().equalsByValueAndUnits(batchModel.getRxnEquivsAmount())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findIfAllWeightAmountsAreSameInTheList() {
        if (this.batchModels.size() < 2) {
            return true;
        } else {
            int size = this.batchModels.size();
            BatchModel refBatchModel = this.batchModels.get(0);
            for (int i = 1; i < size; i++) {
                BatchModel batchModel = this.batchModels.get(i);
                if (!refBatchModel.getWeightAmount().equalsByValueAndUnits(batchModel.getWeightAmount())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findIfAllVolumeAmountsAreSameInTheList() {
        if (this.batchModels.size() < 2) {
            return true;
        } else {
            int size = this.batchModels.size();
            BatchModel refBatchModel = this.batchModels.get(0);
            for (int i = 1; i < size; i++) {
                BatchModel batchModel = this.batchModels.get(i);
                if (!refBatchModel.getVolumeAmount().equalsByValueAndUnits(batchModel.getVolumeAmount())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findIfAllDensityAmountsAreSameInTheList() {
        if (this.batchModels.size() < 2) {
            return true;
        } else {
            int size = this.batchModels.size();
            BatchModel refBatchModel = this.batchModels.get(0);
            for (int i = 1; i < size; i++) {
                BatchModel batchModel = this.batchModels.get(i);
                if (!refBatchModel.getDensityAmount().equalsByValueAndUnits(batchModel.getDensityAmount())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findIfAllPurityAmountsAreSameInTheList() {
        if (this.batchModels.size() < 2) {
            return true;
        } else {
            int size = this.batchModels.size();
            BatchModel refBatchModel = this.batchModels.get(0);
            for (int i = 1; i < size; i++) {
                BatchModel batchModel = this.batchModels.get(i);
                if (!refBatchModel.getPurityAmount().equalsByValueAndUnits(batchModel.getPurityAmount())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateAllListLevelFlags() {
        this.isAllBatchModelsMoleAmountIsSame = this.findIfAllMoleAmountsAreSameInTheList();
        this.isAllBatchModelsWeightAmountIsSame = this.findIfAllWeightAmountsAreSameInTheList();
        this.isAllBatchModelsVolumeAmountIsSame = this.findIfAllVolumeAmountsAreSameInTheList();
        this.isAllBatchModelsDensityAmountIsSame = this.findIfAllDensityAmountsAreSameInTheList();
        this.isAllBatchModelsMolarAmountIsSame = this.findIfAllMolarAmountsAreSameInTheList();
        this.isAllBatchModelsPurityAmountIsSame = this.findIfAllPurityAmountsAreSameInTheList();
        this.isAllBatchModelsRxnEquivsIsSame = this.findIfAllRxnEquivsAmountsAreSameInTheList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BatchesList<?> that = (BatchesList<?>) o;

        if (isAllBatchModelsMoleAmountIsSame != that.isAllBatchModelsMoleAmountIsSame) return false;
        if (isAllBatchModelsWeightAmountIsSame != that.isAllBatchModelsWeightAmountIsSame) return false;
        if (isAllBatchModelsVolumeAmountIsSame != that.isAllBatchModelsVolumeAmountIsSame) return false;
        if (isAllBatchModelsDensityAmountIsSame != that.isAllBatchModelsDensityAmountIsSame) return false;
        if (isAllBatchModelsMolarAmountIsSame != that.isAllBatchModelsMolarAmountIsSame) return false;
        if (isAllBatchModelsPurityAmountIsSame != that.isAllBatchModelsPurityAmountIsSame) return false;
        if (isAllBatchModelsRxnEquivsIsSame != that.isAllBatchModelsRxnEquivsIsSame) return false;
        if (!listMolecularWeightAmount.equals(that.listMolecularWeightAmount))
            return false;
        if (!listMoleAmount.equals(that.listMoleAmount))
            return false;
        if (!listWeightAmount.equals(that.listWeightAmount))
            return false;
        if (!listVolumeAmount.equals(that.listVolumeAmount))
            return false;
        if (!listDensityAmount.equals(that.listDensityAmount))
            return false;
        if (!listMolarAmount.equals(that.listMolarAmount))
            return false;
        if (!listPurityAmount.equals(that.listPurityAmount))
            return false;
        if (!listRxnEquivsAmount.equals(that.listRxnEquivsAmount))
            return false;
        if (!listPreviousMolarAmount.equals(that.listPreviousMolarAmount))
            return false;
        return batchModels.equals(that.batchModels);

    }

    @Override
    public int hashCode() {
        int result = listMolecularWeightAmount.hashCode();
        result = 31 * result + listMoleAmount.hashCode();
        result = 31 * result + listWeightAmount.hashCode();
        result = 31 * result + listVolumeAmount.hashCode();
        result = 31 * result + listDensityAmount.hashCode();
        result = 31 * result + listMolarAmount.hashCode();
        result = 31 * result + listPurityAmount.hashCode();
        result = 31 * result + listRxnEquivsAmount.hashCode();
        result = 31 * result + listPreviousMolarAmount.hashCode();
        result = 31 * result + batchModels.hashCode();
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result;
        result = 31 * result + (isAllBatchModelsMoleAmountIsSame ? 1 : 0);
        result = 31 * result + (isAllBatchModelsWeightAmountIsSame ? 1 : 0);
        result = 31 * result + (isAllBatchModelsVolumeAmountIsSame ? 1 : 0);
        result = 31 * result + (isAllBatchModelsDensityAmountIsSame ? 1 : 0);
        result = 31 * result + (isAllBatchModelsMolarAmountIsSame ? 1 : 0);
        result = 31 * result + (isAllBatchModelsPurityAmountIsSame ? 1 : 0);
        result = 31 * result + (isAllBatchModelsRxnEquivsIsSame ? 1 : 0);
        return result;
    }

    public void resetAmount(String amountName) {
        for (E batchModel1 : batchModels) {
            if (amountName.equals(CeNConstants.MOLE_AMOUNT)) {
                batchModel1.getMoleAmount().reset();
            }
        }
    }
}