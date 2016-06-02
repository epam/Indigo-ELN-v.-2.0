package com.chemistry.enotebook.experiment.datamodel.batch;

public class BatchTypeFactory {
    private BatchTypeFactory() {
    }

    public static BatchType getBatchType(String batchType) throws InvalidBatchTypeException {
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
        if ((batchType.equalsIgnoreCase(BatchType.INTENDED_PRODUCT.toString())) || (batchType.equalsIgnoreCase("INTENDED"))) {
            result = BatchType.INTENDED_PRODUCT;
        }
        if ((batchType.equalsIgnoreCase(BatchType.ACTUAL_PRODUCT.toString())) || (batchType.equalsIgnoreCase("ACTUAL"))) {
            result = BatchType.ACTUAL_PRODUCT;
        }
        if (result == null)
            throw new InvalidBatchTypeException("BatchType value not found: " + batchType);
        return result;
    }
}
