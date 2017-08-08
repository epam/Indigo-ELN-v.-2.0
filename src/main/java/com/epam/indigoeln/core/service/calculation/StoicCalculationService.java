package com.epam.indigoeln.core.service.calculation;

import com.epam.indigoeln.core.chemistry.ProductCalculator;
import com.epam.indigoeln.core.chemistry.StoichCalculator;
import com.epam.indigoeln.core.chemistry.domain.*;
import com.epam.indigoeln.core.chemistry.experiment.datamodel.batch.BatchType;
import com.epam.indigoeln.core.chemistry.experiment.datamodel.batch.BatchTypeFactory;
import com.epam.indigoeln.web.rest.dto.calculation.BasicBatchModel;
import com.epam.indigoeln.web.rest.dto.calculation.ProductTableDTO;
import com.epam.indigoeln.web.rest.dto.calculation.StoicTableDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.ScalarValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.StringValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.UnitValueDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.core.chemistry.experiment.common.units.UnitType.*;

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
        //Force to reacalc Amounts like Weight etc when MWT changes
        if ("molWeight".equals(stoicTableDTO.getChangedField())) {
            batchModel.recalcAmounts();
        }
        StoichCalculator stoichCalculator = new StoichCalculator(reactionStepModel);
        stoichCalculator.recalculateStoichBasedOnBatch(batchModel, false);

        return prepareStoicTableForResponse(reactionStepModel, stoicTableDTO);
    }

    // mimic changes on the field by resetting value and calling setter
    private void callRecalculatingSetterForChangedField(BasicBatchModel sourceBatch, MonomerBatchModel batchModel, String changedField) {
        if (changedField == null) {
            return;
        }
        switch (changedField) {
            case "weight":
                batchModel.setWeightAmountQuitly(new AmountModel(MASS, 0));
                batchModel.setWeightAmount(new AmountModel(MASS, sourceBatch.getWeight().getValue(), !sourceBatch.getWeight().isEntered()));
                break;
            case "volume":
                batchModel.setVolumeAmountQuitly(new AmountModel(VOLUME, 0));
                AmountModel volume = new AmountModel(VOLUME, sourceBatch.getVolume().getValue(), !sourceBatch.getVolume().isEntered());
                batchModel.setVolumeAmount(volume);
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
            default:
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
        if (stoicBatches != null) {
            for (BasicBatchModel sourceMonomerBatch : stoicBatches) {
                MonomerBatchModel monomer = createMonomerBatchModel(sourceMonomerBatch);
                monomer.setStoicTransactionOrder(order);
                stoicBatchesList.addBatch(monomer);
                order++;
            }
        }
        return stoicBatchesList;
    }

    private ArrayList<BatchesList<ProductBatchModel>> prepareProductBatchesList(List<BasicBatchModel> intendedProducts, List<BasicBatchModel> actualProducts) {
        ArrayList<BatchesList<ProductBatchModel>> productBatchesList = new ArrayList<>();
        int intendedOrder = 0;
        int stoicOrder = 0;
        // intended products
        if (intendedProducts != null) {
            for (BasicBatchModel sourceProductBatch : intendedProducts) {
                ProductBatchModel productBatchModel = createProductBatchModelForCalculation(sourceProductBatch);
                BatchesList<ProductBatchModel> productBatches = new BatchesList<>();
                productBatchModel.setBatchType(BatchType.INTENDED_PRODUCT);
                productBatchModel.setIntendedBatchAdditionOrder(intendedOrder);
                productBatches.addBatch(productBatchModel);
                productBatchesList.add(productBatches);
                intendedOrder++;
            }
        }

        // actual products
        if (actualProducts != null) {
            for (BasicBatchModel sourceProductBatch : actualProducts) {
                ProductBatchModel productBatchModel = createProductBatchModelForCalculation(sourceProductBatch);
                BatchesList<ProductBatchModel> productBatches = new BatchesList<>();
                productBatchModel.setBatchType(BatchType.ACTUAL_PRODUCT);
                productBatchModel.setStoicTransactionOrder(stoicOrder);
                productBatches.addBatch(productBatchModel);
                productBatchesList.add(productBatches);
                stoicOrder++;
            }
        }
        return productBatchesList;
    }

    private StoicTableDTO prepareStoicTableForResponse(ReactionStepModel reactionStepModel, StoicTableDTO stoicTableDTO) {
        List<BasicBatchModel> stoicBatches = convertMonomerBatchesListForResponse(reactionStepModel.getBatchesFromStoicBatchesList(), stoicTableDTO.getStoicBatches());
        List<BasicBatchModel> intendedProducts = convertProductBatchesListForResponse(reactionStepModel.getIntendedProductBatches(), stoicTableDTO.getIntendedProducts());
        List<BasicBatchModel> actualProducts = convertProductBatchesListForResponse(reactionStepModel.getActualProductBatches(), stoicTableDTO.getActualProducts());
        return new StoicTableDTO(stoicBatches, intendedProducts, actualProducts);
    }

    private List<BasicBatchModel> convertProductBatchesListForResponse(List<ProductBatchModel> calculatedBatches, List<BasicBatchModel> rawBatches) {
        List<BasicBatchModel> myBatches = new ArrayList<>();
        int index = 0;
        for (ProductBatchModel calculatedBatch : calculatedBatches) {

            BasicBatchModel convertedBatch = prepareBatchModelForResponse(calculatedBatch, rawBatches.get(index));
            convertedBatch.setYield(calculatedBatch.getTheoreticalYieldPercentAmount().doubleValue());
            myBatches.add(convertedBatch);
            index++;
        }
        return myBatches;
    }

    private List<BasicBatchModel> convertMonomerBatchesListForResponse(List<MonomerBatchModel> sourceBatches, List<BasicBatchModel> rawBatches) {
        List<BasicBatchModel> myBatches = new ArrayList<>();
        int index = 0;
        for (MonomerBatchModel sourceBatch : sourceBatches) {
            myBatches.add(prepareBatchModelForResponse(sourceBatch, rawBatches.get(index)));
            index++;
        }
        return myBatches;
    }

    private MonomerBatchModel createMonomerBatchModel(BasicBatchModel rawBatch) {
        MonomerBatchModel batch = new MonomerBatchModel();

        batch.copyAmounts(rawBatch);
        batch.setLastUpdatedType(getLastUpdatedType(rawBatch));
        batch.setPreviousMolarAmount(new AmountModel(MOLAR, rawBatch.getPrevMolarAmount().getValue(), !rawBatch.getPrevMolarAmount().isEntered()));

        return batch;
    }

    private ProductBatchModel createProductBatchModelForCalculation(BasicBatchModel rawBatch) {
        ProductBatchModel batch = new ProductBatchModel(new AmountModel(MASS, rawBatch.getMolWeight().getValue(), !rawBatch.getMolWeight().isEntered()),
                new AmountModel(MOLES, rawBatch.getMol().getValue(), !rawBatch.getMol().isEntered()),
                new AmountModel(MASS, rawBatch.getWeight().getValue(), !rawBatch.getWeight().isEntered()),
                new AmountModel(VOLUME, rawBatch.getVolume().getValue(), !rawBatch.getVolume().isEntered()),
                new AmountModel(DENSITY, rawBatch.getDensity().getValue(), !rawBatch.getDensity().isEntered()),
                new AmountModel(MOLAR, rawBatch.getMolarity().getValue(), !rawBatch.getMolarity().isEntered()),
                new AmountModel(SCALAR, rawBatch.getStoicPurity().getValue(), !rawBatch.getStoicPurity().isEntered()),
                new AmountModel(SCALAR, rawBatch.getEq().getValue(), !rawBatch.getEq().isEntered()),
                rawBatch.isLimiting(),
                BatchTypeFactory.getBatchType(rawBatch.getRxnRole().getName()),
                new AmountModel(VOLUME, rawBatch.getTotalVolume().getValue(), !rawBatch.getTotalVolume().isEntered()),
                new AmountModel(MASS, rawBatch.getTotalWeight().getValue(), !rawBatch.getTotalWeight().isEntered()),
                new AmountModel(MOLES, rawBatch.getTotalMoles().getValue(), !rawBatch.getTotalMoles().isEntered()),
                new AmountModel(SCALAR, rawBatch.getYield()),
                new AmountModel(MASS, rawBatch.getTheoWeight().getValue(), !rawBatch.getTheoWeight().isEntered()),
                new AmountModel(MOLES, rawBatch.getTheoMoles().getValue(), !rawBatch.getTheoMoles().isEntered()));
        batch.setLastUpdatedType(getLastUpdatedType(rawBatch));
        batch.setPreviousMolarAmount(new AmountModel(MOLAR, rawBatch.getPrevMolarAmount().getValue(), !rawBatch.getPrevMolarAmount().isEntered()));
        return batch;
    }

    private BasicBatchModel prepareBatchModelForResponse(BatchModel calculatedBatch, BasicBatchModel rawBatch) {
        rawBatch.setMolWeight(createScalarValue(calculatedBatch.getMolecularWeightAmount(), rawBatch.getMolWeight()));
        rawBatch.setMol(createUnitValue(calculatedBatch.getMoleAmount(), rawBatch.getMol()));
        rawBatch.setMolarity(createUnitValue(calculatedBatch.getMolarAmount(), rawBatch.getMolarity()));
        rawBatch.setStoicPurity(createScalarValue(calculatedBatch.getPurityAmount(), rawBatch.getStoicPurity()));
        rawBatch.setWeight(createUnitValue(calculatedBatch.getWeightAmount(), rawBatch.getWeight()));
        rawBatch.setVolume(createUnitValue(calculatedBatch.getVolumeAmount(), rawBatch.getVolume()));
        rawBatch.setDensity(createUnitValue(calculatedBatch.getDensityAmount(), rawBatch.getDensity()));
        rawBatch.setRxnRole(new StringValueDTO(rawBatch.getRxnRole().getName(), rawBatch.getRxnRole().getName(), false, false));
        rawBatch.setEq(createScalarValue(calculatedBatch.getRxnEquivsAmount(), rawBatch.getEq()));
        rawBatch.setLimiting(calculatedBatch.isLimiting());
        rawBatch.setTotalWeight(createUnitValue(calculatedBatch.getTotalWeight(), rawBatch.getTotalWeight()));
        rawBatch.setTotalVolume(createUnitValue(calculatedBatch.getTotalVolume(), rawBatch.getTotalVolume()));
        rawBatch.setTotalMoles(createUnitValue(calculatedBatch.getTotalMolarity(), rawBatch.getTotalMoles()));
        if (calculatedBatch instanceof ProductBatchModel && calculatedBatch.getBatchType().equals(BatchType.ACTUAL_PRODUCT)) {
            rawBatch.setTheoMoles(createUnitValue(calculatedBatch.getTheoreticalMoleAmount(), rawBatch.getTheoMoles()));
            rawBatch.setTheoWeight(createUnitValue(calculatedBatch.getTheoreticalWeightAmount(), rawBatch.getTheoWeight()));
        } else if (calculatedBatch instanceof ProductBatchModel && calculatedBatch.getBatchType().equals(BatchType.INTENDED_PRODUCT)) {
            // to sync intended products with actual products
            rawBatch.setTheoMoles(createUnitValue(calculatedBatch.getMoleAmount(), rawBatch.getMol()));
            rawBatch.setTheoWeight(createUnitValue(calculatedBatch.getWeightAmount(), rawBatch.getWeight()));
        }
        rawBatch.setLastUpdatedType(convertLastUpdatedTypeForResponse(calculatedBatch.getLastUpdatedType()));
        rawBatch.setPrevMolarAmount(createUnitValue(calculatedBatch.getPreviousMolarAmount(), rawBatch.getPrevMolarAmount()));
        return rawBatch;
    }

    private ScalarValueDTO createScalarValue(AmountModel calculatedBatchValue, ScalarValueDTO rawBatchValue) {
        return new ScalarValueDTO(calculatedBatchValue.doubleValue(), rawBatchValue.getDisplayValue(), !calculatedBatchValue.isCalculated(), rawBatchValue.isReadonly());
    }

    private UnitValueDTO createUnitValue(AmountModel calculatedBatchValue, UnitValueDTO rawBatchValue) {
        return new UnitValueDTO(calculatedBatchValue.doubleValue(), rawBatchValue.getDisplayValue(),
                getUnit(calculatedBatchValue, rawBatchValue), !calculatedBatchValue.isCalculated(), rawBatchValue.isReadonly());
    }

    private String getUnit(AmountModel calculatedBatchValue, UnitValueDTO rawBatchValue) {
        return StringUtils.isBlank(rawBatchValue.getUnit()) ? calculatedBatchValue.getUnit().getDisplayValue() : rawBatchValue.getUnit();
    }

    private int getLastUpdatedType(BasicBatchModel rawBatch) {
        int lastUpdatedType = 0;
        if (rawBatch.getLastUpdatedType() != null) {
            switch (rawBatch.getLastUpdatedType()) {
                case "mol":
                    lastUpdatedType = BatchModel.UPDATE_TYPE_MOLES;
                    break;
                case "weight":
                    lastUpdatedType = BatchModel.UPDATE_TYPE_WEIGHT;
                    break;
                case "volume":
                    lastUpdatedType = BatchModel.UPDATE_TYPE_VOLUME;
                    break;
                case "totalWeight":
                    lastUpdatedType = BatchModel.UPDATE_TYPE_TOTAL_WEIGHT;
                    break;
                case "totalVolume":
                    lastUpdatedType = BatchModel.UPDATE_TYPE_TOTAL_VOLUME;
                    break;
                default:
                    break;
            }
        }
        return lastUpdatedType;
    }

    private String convertLastUpdatedTypeForResponse(int lastUpdatedType) {
        String result = "";
        switch (lastUpdatedType) {
            case BatchModel.UPDATE_TYPE_MOLES:
                result = "mol";
                break;
            case BatchModel.UPDATE_TYPE_WEIGHT:
                result = "weight";
                break;
            case BatchModel.UPDATE_TYPE_VOLUME:
                result = "volume";
                break;
            case BatchModel.UPDATE_TYPE_TOTAL_WEIGHT:
                result = "totalWeight";
                break;
            case BatchModel.UPDATE_TYPE_TOTAL_VOLUME:
                result = "totalVolume";
                break;
            default:
                break;
        }
        return result;
    }

    public BasicBatchModel calculateProductBatch(ProductTableDTO productTableDTO) {
        ProductCalculator productCalculator = new ProductCalculator();
        BasicBatchModel rawBatch = productTableDTO.getProductBatch();
        ProductBatchModel productBatch = createProductBatchModelForCalculation(rawBatch);
        String changedField = productTableDTO.getChangedField();
        productCalculator.calculateProductBatch(productBatch, rawBatch, changedField);

        BasicBatchModel convertedBatch = prepareBatchModelForResponse(productBatch, rawBatch);
        convertedBatch.setYield(productBatch.getTheoreticalYieldPercentAmount().doubleValue());
        return convertedBatch;
    }

    public BasicBatchModel recalculateBatchAmounts(ProductTableDTO productTableDTO) {
        BasicBatchModel rawBatch = productTableDTO.getProductBatch();
        ProductBatchModel productBatch = createProductBatchModelForCalculation(rawBatch);
        productBatch.recalcAmounts();

        BasicBatchModel convertedBatch = prepareBatchModelForResponse(productBatch, rawBatch);
        convertedBatch.setYield(productBatch.getTheoreticalYieldPercentAmount().doubleValue());
        return convertedBatch;
    }
}