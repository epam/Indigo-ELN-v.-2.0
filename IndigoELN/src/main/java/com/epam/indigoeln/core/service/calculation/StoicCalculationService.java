package com.epam.indigoeln.core.service.calculation;

import com.chemistry.enotebook.ProductCalculator;
import com.chemistry.enotebook.StoichCalculator;
import com.chemistry.enotebook.domain.*;
import com.chemistry.enotebook.experiment.datamodel.batch.BatchType;
import com.chemistry.enotebook.experiment.datamodel.batch.BatchTypeFactory;
import com.epam.indigoeln.web.rest.dto.calculation.BasicBatchModel;
import com.epam.indigoeln.web.rest.dto.calculation.ProductTableDTO;
import com.epam.indigoeln.web.rest.dto.calculation.StoicTableDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.ScalarValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.StringValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.UnitValueDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.chemistry.enotebook.experiment.common.units.UnitType.*;

/**
 * Service for stoichiometry calculations
 */
@Service
public class StoicCalculationService {

    public StoicTableDTO calculateStoicTable(StoicTableDTO stoicTableDTO) {
        ReactionStepModel reactionStepModel = createReactionStepModelForCalculation(stoicTableDTO);
        StoichCalculator stoichCalculator = new StoichCalculator(reactionStepModel);
        stoichCalculator.recalculateStoich();
        return prepareStoicTableForResponse(reactionStepModel, stoicTableDTO);
    }

    /**
     * Recalculates stoic table batches, actual product and intended product batches
     *
     * @param stoicTableDTO contains stoic table batches, actual product batches (product batch summary)
     *                      and intended product batches
     * @return stoicTableDTO with recalculated batches
     */
    public StoicTableDTO calculateStoicTableBasedOnBatch(StoicTableDTO stoicTableDTO) {
        ReactionStepModel reactionStepModel = createReactionStepModelForCalculation(stoicTableDTO);

        String changedField = stoicTableDTO.getChangedField();
        BasicBatchModel sourceBatch = stoicTableDTO.getStoicBatches().get(stoicTableDTO.getChangedBatchRowNumber());
        MonomerBatchModel batchModel = (MonomerBatchModel) reactionStepModel.getStoicElementListInTransactionOrder().get(stoicTableDTO.getChangedBatchRowNumber());
        callRecalculatingSetterForChangedField(sourceBatch, batchModel, changedField);

        StoichCalculator stoichCalculator = new StoichCalculator(reactionStepModel);
        stoichCalculator.recalculateStoichBasedOnBatch(batchModel, false);

        return prepareStoicTableForResponse(reactionStepModel, stoicTableDTO);
    }

    // mimic changes on the field by resetting value and calling setter
    private void callRecalculatingSetterForChangedField(BasicBatchModel sourceBatch, MonomerBatchModel batchModel, String changedField) {
        switch (changedField) {
            case "weight":
                batchModel.setWeightAmountQuitly(new AmountModel(MASS, 0));
                batchModel.setWeightAmount(new AmountModel(MASS, sourceBatch.getWeight().getValue(), !sourceBatch.getWeight().isEntered()));
                break;
            case "volume":
                batchModel.setVolumeAmountQuitly(new AmountModel(VOLUME, 0));
                batchModel.setVolumeAmount(new AmountModel(VOLUME, sourceBatch.getVolume().getValue(), !sourceBatch.getVolume().isEntered()));
                break;
            case "molarity":
                batchModel.setMolarAmountQuitly(new AmountModel(MOLAR, 0));
                batchModel.setMolarAmount(new AmountModel(MOLAR, sourceBatch.getMolarity().getValue(), !sourceBatch.getMolarity().isEntered()));
                break;
            case "mol":
                batchModel.setMoleAmountQuitly(new AmountModel(MOLES, 0));
                batchModel.setMoleAmount(new AmountModel(MOLES, sourceBatch.getMol().getValue(), !sourceBatch.getMol().isEntered()));
                break;
            case "eq":
                batchModel.setRxnEquivsAmountQuitly(new AmountModel(SCALAR, 1));
                batchModel.setRxnEquivsAmount(new AmountModel(SCALAR, sourceBatch.getEq().getValue(), !sourceBatch.getEq().isEntered()));
                break;
            case "stoicPurity":
                batchModel.setPurityAmountQuitly(new AmountModel(SCALAR, 100));
                batchModel.setPurityAmount(new AmountModel(SCALAR, sourceBatch.getStoicPurity().getValue(), !sourceBatch.getStoicPurity().isEntered()));
                break;
            case "density":
                batchModel.setDensityAmountQuitly(new AmountModel(DENSITY, 0));
                batchModel.setDensityAmount(new AmountModel(DENSITY, sourceBatch.getDensity().getValue(), !sourceBatch.getDensity().isEntered()));
                break;
        }
    }

    private ReactionStepModel createReactionStepModelForCalculation(StoicTableDTO data) {
        List<BasicBatchModel> stoicBatches = data.getStoicBatches();
        List<BasicBatchModel> intendedProducts = data.getIntendedProducts();
        List<BasicBatchModel> actualProducts = data.getActualProducts();

        BatchesList<MonomerBatchModel> stoicBatchesList = prepareStoicBatchesList(stoicBatches);
        ArrayList<BatchesList<ProductBatchModel>> productBatchesList = prepareProductBatchesList(intendedProducts, actualProducts);

        ReactionStepModel reactionStepModel = new ReactionStepModel();
        reactionStepModel.setStoicBatchesList(stoicBatchesList);
        reactionStepModel.setProducts(productBatchesList);
        return reactionStepModel;
    }

    private BatchesList<MonomerBatchModel> prepareStoicBatchesList(List<BasicBatchModel> stoicBatches) {
        BatchesList<MonomerBatchModel> stoicBatchesList = new BatchesList<>();
        int order = 0;
        for (BasicBatchModel sourceMonomerBatch : stoicBatches) {
            MonomerBatchModel monomer = createMonomerBatchModel(sourceMonomerBatch);
            monomer.setStoicTransactionOrder(order);
            stoicBatchesList.addBatch(monomer);
            order++;
        }
        return stoicBatchesList;
    }

    private ArrayList<BatchesList<ProductBatchModel>> prepareProductBatchesList(List<BasicBatchModel> intendedProducts, List<BasicBatchModel> actualProducts) {
        ArrayList<BatchesList<ProductBatchModel>> productBatchesList = new ArrayList<>();
        int intendedOrder = 0;
        int stoicOrder = 0;
        // intended products
        for (BasicBatchModel sourceProductBatch : intendedProducts) {
            ProductBatchModel productBatchModel = createProductBatchModelForCalculation(sourceProductBatch);
            BatchesList<ProductBatchModel> productBatches = new BatchesList<>();
            productBatchModel.setBatchType(BatchType.INTENDED_PRODUCT);
            productBatchModel.setIntendedBatchAdditionOrder(intendedOrder);
            productBatches.addBatch(productBatchModel);
            productBatchesList.add(productBatches);
            intendedOrder++;
        }

        // actual products
        for (BasicBatchModel sourceProductBatch : actualProducts) {
            ProductBatchModel productBatchModel = createProductBatchModelForCalculation(sourceProductBatch);
            BatchesList<ProductBatchModel> productBatches = new BatchesList<>();
            productBatchModel.setBatchType(BatchType.ACTUAL_PRODUCT);
            productBatchModel.setStoicTransactionOrder(stoicOrder);
            productBatches.addBatch(productBatchModel);
            productBatchesList.add(productBatches);
            stoicOrder++;
        }
        return productBatchesList;
    }

    private StoicTableDTO prepareStoicTableForResponse(ReactionStepModel reactionStepModel, StoicTableDTO stoicTableDTO) {
        List<BasicBatchModel> stoicBatches = convertMonomerBatchesListForResponse(reactionStepModel.getBatchesFromStoicBatchesList(), stoicTableDTO.getStoicBatches());
        List<BasicBatchModel> intendedProducts = convertProductBatchesListForResponse(reactionStepModel.getIntendedProductBatches(), stoicTableDTO.getIntendedProducts());
        List<BasicBatchModel> actualProducts = convertProductBatchesListForResponse(reactionStepModel.getActualProductBatches(), stoicTableDTO.getActualProducts());
        return new StoicTableDTO(stoicBatches, intendedProducts, actualProducts);
    }

    private List<BasicBatchModel> convertProductBatchesListForResponse(List<ProductBatchModel> sourceBatches, List<BasicBatchModel> rawBatches) {
        List<BasicBatchModel> myBatches = new ArrayList<>();
        int index = 0;
        for (ProductBatchModel sourceBatch : sourceBatches) {
            BasicBatchModel converdedBatch = createBatchModelForResponse(sourceBatch, rawBatches.get(index));
            converdedBatch.setYield(sourceBatch.getTheoreticalYieldPercentAmount().doubleValue());
            myBatches.add(converdedBatch);
            index++;
        }
        return myBatches;
    }

    private List<BasicBatchModel> convertMonomerBatchesListForResponse(List<MonomerBatchModel> sourceBatches, List<BasicBatchModel> rawBatches) {
        List<BasicBatchModel> myBatches = new ArrayList<>();
        int index = 0;
        for (MonomerBatchModel sourceBatch : sourceBatches) {
            myBatches.add(createBatchModelForResponse(sourceBatch, rawBatches.get(index)));
            index++;
        }
        return myBatches;
    }

    private MonomerBatchModel createMonomerBatchModel(BasicBatchModel sourceBatch) {
        return new MonomerBatchModel(new AmountModel(MASS, sourceBatch.getMolWeight().getValue(), !sourceBatch.getMolWeight().isEntered()),
                new AmountModel(MOLES, sourceBatch.getMol().getValue(), !sourceBatch.getMol().isEntered()),
                new AmountModel(MASS, sourceBatch.getWeight().getValue(), !sourceBatch.getWeight().isEntered()),
                new AmountModel(VOLUME, sourceBatch.getVolume().getValue(), !sourceBatch.getVolume().isEntered()),
                new AmountModel(DENSITY, sourceBatch.getDensity().getValue(), !sourceBatch.getDensity().isEntered()),
                new AmountModel(MOLAR, sourceBatch.getMolarity().getValue(), !sourceBatch.getMolarity().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getStoicPurity().getValue(), !sourceBatch.getStoicPurity().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getEq().getValue(), !sourceBatch.getEq().isEntered()),
                sourceBatch.isLimiting(),
                BatchTypeFactory.getBatchType(sourceBatch.getRxnRole().getName()),
                new AmountModel(VOLUME, sourceBatch.getTotalVolume().getValue(), !sourceBatch.getTotalVolume().isEntered()),
                new AmountModel(MASS, sourceBatch.getTotalWeight().getValue(), !sourceBatch.getTotalWeight().isEntered()),
                new AmountModel(MOLES, sourceBatch.getTotalMoles().getValue(), !sourceBatch.getTotalMoles().isEntered()));
    }

    private ProductBatchModel createProductBatchModelForCalculation(BasicBatchModel sourceBatch) {
        return new ProductBatchModel(new AmountModel(MASS, sourceBatch.getMolWeight().getValue(), !sourceBatch.getMolWeight().isEntered()),
                new AmountModel(MOLES, sourceBatch.getMol().getValue(), !sourceBatch.getMol().isEntered()),
                new AmountModel(MASS, sourceBatch.getWeight().getValue(), !sourceBatch.getWeight().isEntered()),
                new AmountModel(VOLUME, sourceBatch.getVolume().getValue(), !sourceBatch.getVolume().isEntered()),
                new AmountModel(DENSITY, sourceBatch.getDensity().getValue(), !sourceBatch.getDensity().isEntered()),
                new AmountModel(MOLAR, sourceBatch.getMolarity().getValue(), !sourceBatch.getMolarity().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getStoicPurity().getValue(), !sourceBatch.getStoicPurity().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getEq().getValue(), !sourceBatch.getEq().isEntered()),
                sourceBatch.isLimiting(),
                BatchTypeFactory.getBatchType(sourceBatch.getRxnRole().getName()),
                new AmountModel(VOLUME, sourceBatch.getTotalVolume().getValue(), !sourceBatch.getTotalVolume().isEntered()),
                new AmountModel(MASS, sourceBatch.getTotalWeight().getValue(), !sourceBatch.getTotalWeight().isEntered()),
                new AmountModel(MOLES, sourceBatch.getTotalMoles().getValue(), !sourceBatch.getTotalMoles().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getYield()),
                new AmountModel(MASS, sourceBatch.getTheoWeight().getValue(), !sourceBatch.getTheoWeight().isEntered()),
                new AmountModel(MOLES, sourceBatch.getTheoMoles().getValue(), !sourceBatch.getTheoMoles().isEntered()));
    }

    private BasicBatchModel createBatchModelForResponse(BatchModel sourceBatch, BasicBatchModel rawBatch) {
        rawBatch.setMolWeight(new ScalarValueDTO(sourceBatch.getMolecularWeightAmount().doubleValue(), !sourceBatch.getMolecularWeightAmount().isCalculated(), rawBatch.getMolWeight().isReadonly())); //todo check unit type for mW (g/mol)
        rawBatch.setMol(new UnitValueDTO(sourceBatch.getMoleAmount().doubleValue(), sourceBatch.getMoleAmount().getUnit().getDisplayValue(), !sourceBatch.getMoleAmount().isCalculated(), rawBatch.getMol().isReadonly())); // todo check, should be mass to volume?
        rawBatch.setMolarity(new UnitValueDTO(sourceBatch.getMolarAmount().doubleValue(), sourceBatch.getMolarAmount().getUnit().getDisplayValue(), !sourceBatch.getMolarAmount().isCalculated(), rawBatch.getMolarity().isReadonly()));
        rawBatch.setStoicPurity(new ScalarValueDTO(sourceBatch.getPurityAmount().doubleValue(), !sourceBatch.getPurityAmount().isCalculated(), rawBatch.getStoicPurity().isReadonly()));
        rawBatch.setWeight(new UnitValueDTO(sourceBatch.getWeightAmount().doubleValue(), sourceBatch.getWeightAmount().getUnit().getDisplayValue(), !sourceBatch.getWeightAmount().isCalculated(), rawBatch.getWeight().isReadonly()));
        rawBatch.setVolume(new UnitValueDTO(sourceBatch.getVolumeAmount().doubleValue(), sourceBatch.getVolumeAmount().getUnit().getDisplayValue(), !sourceBatch.getVolumeAmount().isCalculated(), rawBatch.getVolume().isReadonly()));
        rawBatch.setDensity(new UnitValueDTO(sourceBatch.getDensityAmount().doubleValue(), sourceBatch.getDensityAmount().getUnit().getDisplayValue(), !sourceBatch.getDensityAmount().isCalculated(), rawBatch.getDensity().isReadonly()));
        rawBatch.setRxnRole(new StringValueDTO(sourceBatch.getBatchType().toString(), false, false));
        rawBatch.setEq(new ScalarValueDTO(sourceBatch.getRxnEquivsAmount().doubleValue(), !sourceBatch.getRxnEquivsAmount().isCalculated(), rawBatch.getEq().isReadonly()));
        rawBatch.setLimiting(sourceBatch.isLimiting());
        rawBatch.setTotalWeight(new UnitValueDTO(sourceBatch.getTotalWeight().doubleValue(), sourceBatch.getTotalWeight().getUnit().getDisplayValue(), !sourceBatch.getTotalWeight().isCalculated(), rawBatch.getTotalWeight().isReadonly()));
        rawBatch.setTotalVolume(new UnitValueDTO(sourceBatch.getTotalVolume().doubleValue(), sourceBatch.getTotalVolume().getUnit().getDisplayValue(), !sourceBatch.getTotalVolume().isCalculated(), rawBatch.getTotalVolume().isReadonly()));
        rawBatch.setTotalMoles(new UnitValueDTO(sourceBatch.getTotalMolarity().doubleValue(), sourceBatch.getTotalMolarity().getUnit().getDisplayValue(), !sourceBatch.getTotalMolarity().isCalculated(), rawBatch.getTotalMoles().isReadonly())); // todo check, should be mass to volume?
        if (sourceBatch instanceof ProductBatchModel) {
            rawBatch.setTheoMoles(new UnitValueDTO(sourceBatch.getTheoreticalMoleAmount().doubleValue(), sourceBatch.getTheoreticalMoleAmount().getUnit().getDisplayValue(), !sourceBatch.getTheoreticalMoleAmount().isCalculated(), rawBatch.getTheoMoles().isReadonly()));
            rawBatch.setTheoWeight(new UnitValueDTO(sourceBatch.getTheoreticalWeightAmount().doubleValue(), sourceBatch.getTheoreticalWeightAmount().getUnit().getDisplayValue(), !sourceBatch.getTheoreticalWeightAmount().isCalculated(), rawBatch.getTheoWeight().isReadonly()));
        }
        return rawBatch;
    }

    public BasicBatchModel calculateProductBatch(ProductTableDTO productTableDTO) {
        ProductCalculator productCalculator = new ProductCalculator();
        BasicBatchModel rawBatch = productTableDTO.getProductBatch();
        ProductBatchModel productBatch = createProductBatchModelForCalculation(rawBatch);
        String changedField = productTableDTO.getChangedField();
        productCalculator.calculateProductBatch(productBatch, rawBatch, changedField);

        BasicBatchModel convertedBatch = createBatchModelForResponse(productBatch, rawBatch);
        convertedBatch.setYield(productBatch.getTheoreticalYieldPercentAmount().doubleValue());
        return convertedBatch;
    }

}