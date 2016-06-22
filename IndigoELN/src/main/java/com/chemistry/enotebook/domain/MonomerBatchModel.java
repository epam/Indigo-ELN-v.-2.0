package com.chemistry.enotebook.domain;

import com.chemistry.enotebook.experiment.datamodel.batch.BatchType;

public class MonomerBatchModel extends BatchModel {

    public static final long serialVersionUID = 7526472295622776147L;

    public MonomerBatchModel() {
    }

    public MonomerBatchModel(AmountModel molecularWeightAmount, AmountModel moleAmount, AmountModel weightAmount, AmountModel volumeAmount, AmountModel densityAmount, AmountModel molarAmount, AmountModel purityAmount, AmountModel rxnEquivsAmount, boolean limiting, BatchType batchType, AmountModel totalVolume, AmountModel totalWeight, AmountModel totalMolarity) {
        super(molecularWeightAmount, moleAmount, weightAmount, volumeAmount, densityAmount, molarAmount, purityAmount, rxnEquivsAmount, limiting, batchType, totalVolume, totalWeight, totalMolarity);
    }

}