package com.chemistry.enotebook.experiment.datamodel.batch;

public class BatchTypeFactory {
    private BatchTypeFactory() {
    }

    public static BatchType getBatchType(String batchType) {
        BatchType result = null;
        if (batchType.equalsIgnoreCase(BatchType.REAGENT.toString())) {
            result = BatchType.REAGENT;
        }
        if (batchType.equalsIgnoreCase(BatchType.SOLVENT.toString())) {
            result = BatchType.SOLVENT;
        }
        if (batchType.equalsIgnoreCase(BatchType.REACTANT.toString())
                || batchType.equalsIgnoreCase(BatchType.START_MTRL.toString())) {
            result = BatchType.REACTANT;
        }
        if ((batchType.equalsIgnoreCase(BatchType.INTENDED_PRODUCT.toString())) || ("INTENDED".equalsIgnoreCase(batchType))) {
            result = BatchType.INTENDED_PRODUCT;
        }
        if ((batchType.equalsIgnoreCase(BatchType.ACTUAL_PRODUCT.toString())) || ("ACTUAL".equalsIgnoreCase(batchType))) {
            result = BatchType.ACTUAL_PRODUCT;
        }
        return result;
    }
}
