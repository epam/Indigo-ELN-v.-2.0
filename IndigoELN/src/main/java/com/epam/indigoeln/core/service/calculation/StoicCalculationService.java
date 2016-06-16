package com.epam.indigoeln.core.service.calculation;

import com.chemistry.enotebook.StoichCalculator;
import com.chemistry.enotebook.domain.*;
import com.chemistry.enotebook.experiment.datamodel.batch.BatchType;
import com.chemistry.enotebook.experiment.datamodel.batch.BatchTypeFactory;
import com.epam.indigoeln.web.rest.dto.calculation.StoicBatchDTO;
import com.epam.indigoeln.web.rest.dto.calculation.StoicTableDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.ScalarValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.StringValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.UnitValueDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.chemistry.enotebook.experiment.common.units.UnitType.*;

/**
 * Service for stoichiometry calculations
 */
@Service
public class StoicCalculationService {

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Recalculates stoic table batches, actual product and intended product batches
     *
     * @param stoicTableDTO contains stoic table batches, actual product batches (product batch summary)
     *                      and intended product batches
     * @return stoicTableDTO with recalculated batches
     */
    public StoicTableDTO calculateStoicTable(StoicTableDTO stoicTableDTO) {
        try {
            String result = objectMapper.writeValueAsString(stoicTableDTO);
            System.out.println(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        StoicBatchDTO sourceBatch = stoicTableDTO.getChangedBatch();
        String changedField = stoicTableDTO.getChangedField();
        MonomerBatchModel batchModel = createMonomerBatchModel(sourceBatch);
        callRecalculatingSetterForChangedField(sourceBatch, batchModel, changedField);

        ReactionStepModel reactionStepModel = createReactionStepModelForCalculation(stoicTableDTO);

        StoichCalculator stoichCalculator = new StoichCalculator(reactionStepModel);

        stoichCalculator.recalculateStoichBasedOnBatch(batchModel, false);

        try {
            System.out.println(objectMapper.writeValueAsString(batchModel));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return prepareStoicTableForResponse(reactionStepModel);
    }

    private void callRecalculatingSetterForChangedField(StoicBatchDTO sourceBatch, MonomerBatchModel batchModel, String changedField) {
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
        List<StoicBatchDTO> stoicBatches = data.getStoicBatches();
        List<StoicBatchDTO> intendedProducts = data.getIntendedProducts();
        List<StoicBatchDTO> actualProducts = data.getActualProducts();

        BatchesList<MonomerBatchModel> stoicBatchesList = prepareStoicBatchesList(stoicBatches);
        ArrayList<BatchesList<ProductBatchModel>> productBatchesList = prepareProductBatchesList(intendedProducts, actualProducts);

        ReactionStepModel reactionStepModel = new ReactionStepModel();
        reactionStepModel.setStoicBatchesList(stoicBatchesList);
        reactionStepModel.setProducts(productBatchesList);
        return reactionStepModel;
    }

    private BatchesList<MonomerBatchModel> prepareStoicBatchesList(List<StoicBatchDTO> stoicBatches) {
        BatchesList<MonomerBatchModel> stoicBatchesList = new BatchesList<>();
        for (StoicBatchDTO sourceMonomerBatch : stoicBatches) {
            MonomerBatchModel monomer = createMonomerBatchModel(sourceMonomerBatch);
            stoicBatchesList.addBatch(monomer);
        }
        return stoicBatchesList;
    }

    private ArrayList<BatchesList<ProductBatchModel>> prepareProductBatchesList(List<StoicBatchDTO> intendedProducts, List<StoicBatchDTO> actualProducts) {
        ArrayList<BatchesList<ProductBatchModel>> productBatchesList = new ArrayList<>();
        // intended products
        for (StoicBatchDTO sourceProductBatch : intendedProducts) {
            ProductBatchModel productBatchModel = createProductBatchModelForCalculation(sourceProductBatch);
            BatchesList<ProductBatchModel> productBatches = new BatchesList<>();
            productBatchModel.setBatchType(BatchType.INTENDED_PRODUCT);
            productBatches.addBatch(productBatchModel);
            productBatchesList.add(productBatches);
        }

        // actual products
        for (StoicBatchDTO sourceProductBatch : actualProducts) {
            ProductBatchModel productBatchModel = createProductBatchModelForCalculation(sourceProductBatch);
            BatchesList<ProductBatchModel> productBatches = new BatchesList<>();
            productBatchModel.setBatchType(BatchType.ACTUAL_PRODUCT);
            productBatches.addBatch(productBatchModel);
            productBatchesList.add(productBatches);
        }
        return productBatchesList;
    }

    private StoicTableDTO prepareStoicTableForResponse(ReactionStepModel reactionStepModel) {
        List<StoicBatchDTO> stoicBatches = convertMonomerBatchesListForResponse(reactionStepModel.getBatchesFromStoicBatchesList());
        List<StoicBatchDTO> intendedProducts = convertProductBatchesListForResponse(reactionStepModel.getIntendedProductBatches());
        List<StoicBatchDTO> actualProducts = convertProductBatchesListForResponse(reactionStepModel.getActualProductBatches());
        return new StoicTableDTO(stoicBatches, intendedProducts, actualProducts);
    }

    private List<StoicBatchDTO> convertProductBatchesListForResponse(List<ProductBatchModel> sourceBatches) {
        List<StoicBatchDTO> myBatches = new ArrayList<>();
        for (ProductBatchModel sourceBatch : sourceBatches) {
            myBatches.add(convertBatchModelForResponse(sourceBatch, new StoicBatchDTO()));
        }
        return myBatches;
    }

    private List<StoicBatchDTO> convertMonomerBatchesListForResponse(List<MonomerBatchModel> sourceBatches) {
        List<StoicBatchDTO> myBatches = new ArrayList<>();
        for (MonomerBatchModel sourceBatch : sourceBatches) {
            myBatches.add(convertBatchModelForResponse(sourceBatch, new StoicBatchDTO()));
        }
        return myBatches;
    }

    private MonomerBatchModel createMonomerBatchModel(StoicBatchDTO sourceBatch) {
        return new MonomerBatchModel(new AmountModel(MASS, sourceBatch.getMolWeight().getValue(), !sourceBatch.getMolWeight().isEntered()),
                new AmountModel(MOLES, sourceBatch.getMol().getValue(), !sourceBatch.getMol().isEntered()),
                new AmountModel(MASS, sourceBatch.getWeight().getValue(), !sourceBatch.getWeight().isEntered()),
                new AmountModel(VOLUME, sourceBatch.getVolume().getValue(), !sourceBatch.getVolume().isEntered()),
                new AmountModel(DENSITY, sourceBatch.getDensity().getValue(), !sourceBatch.getDensity().isEntered()),
                new AmountModel(MOLAR, sourceBatch.getMolarity().getValue(), !sourceBatch.getMolarity().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getStoicPurity().getValue(), !sourceBatch.getStoicPurity().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getEq().getValue(), !sourceBatch.getEq().isEntered()),
                sourceBatch.isLimiting(),
                BatchTypeFactory.getBatchType(sourceBatch.getRxnRole().getName()));
    }

    private ProductBatchModel createProductBatchModelForCalculation(StoicBatchDTO sourceBatch) {
        return new ProductBatchModel(new AmountModel(MASS, sourceBatch.getMolWeight().getValue(), !sourceBatch.getMolWeight().isEntered()),
                new AmountModel(MOLES, sourceBatch.getMol().getValue(), !sourceBatch.getMol().isEntered()),
                new AmountModel(MASS, sourceBatch.getWeight().getValue(), !sourceBatch.getWeight().isEntered()),
                new AmountModel(VOLUME, sourceBatch.getVolume().getValue(), !sourceBatch.getVolume().isEntered()),
                new AmountModel(DENSITY, sourceBatch.getDensity().getValue(), !sourceBatch.getDensity().isEntered()),
                new AmountModel(MOLAR, sourceBatch.getMolarity().getValue(), !sourceBatch.getMolarity().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getStoicPurity().getValue(), !sourceBatch.getStoicPurity().isEntered()),
                new AmountModel(SCALAR, sourceBatch.getEq().getValue(), !sourceBatch.getEq().isEntered()),
                sourceBatch.isLimiting(),
                BatchTypeFactory.getBatchType(sourceBatch.getRxnRole().getName()));
    }

    private StoicBatchDTO convertBatchModelForResponse(BatchModel sourceBatch, StoicBatchDTO targetBatch) {
        targetBatch.setMolWeight(new ScalarValueDTO(sourceBatch.getMolecularWeightAmount().doubleValue(), !sourceBatch.getMolecularWeightAmount().isCalculated())); //todo check unit type for mW (g/mol)
        targetBatch.setMol(new UnitValueDTO(sourceBatch.getMoleAmount().doubleValue(), sourceBatch.getMoleAmount().getUnit().getDisplayValue(), !sourceBatch.getMoleAmount().isCalculated())); // todo check, should be mass to volume?
        targetBatch.setMolarity(new UnitValueDTO(sourceBatch.getMolarAmount().doubleValue(), sourceBatch.getMolarAmount().getUnit().getDisplayValue(), !sourceBatch.getMolarAmount().isCalculated()));
        targetBatch.setStoicPurity(new ScalarValueDTO(sourceBatch.getPurityAmount().doubleValue(), !sourceBatch.getPurityAmount().isCalculated()));
        targetBatch.setWeight(new UnitValueDTO(sourceBatch.getWeightAmount().doubleValue(), sourceBatch.getWeightAmount().getUnit().getDisplayValue(), !sourceBatch.getWeightAmount().isCalculated()));
        targetBatch.setVolume(new UnitValueDTO(sourceBatch.getVolumeAmount().doubleValue(), sourceBatch.getVolumeAmount().getUnit().getDisplayValue(), !sourceBatch.getVolumeAmount().isCalculated()));
        targetBatch.setDensity(new UnitValueDTO(sourceBatch.getDensityAmount().doubleValue(), sourceBatch.getDensityAmount().getUnit().getDisplayValue(), !sourceBatch.getDensityAmount().isCalculated()));
        targetBatch.setRxnRole(new StringValueDTO(sourceBatch.getBatchType().toString(), false));
        targetBatch.setEq(new ScalarValueDTO(sourceBatch.getRxnEquivsAmount().doubleValue(), !sourceBatch.getRxnEquivsAmount().isCalculated())); // todo check rxnEquivsAmount or stoicRxnEquivsAmount
        targetBatch.setLimiting(sourceBatch.isLimiting());
//        targetBatch.setTheoMoles(new UnitValueDTO(sourceBatch.getTheoreticalMoleAmount().doubleValue(), sourceBatch.getTheoreticalMoleAmount().getUnit().getDisplayValue()));
//        targetBatch.setTheoWeight(new UnitValueDTO(sourceBatch.getTheoreticalWeightAmount().doubleValue(), sourceBatch.getTheoreticalWeightAmount().getUnit().getDisplayValue()));
        // + yield
        return targetBatch;
    }
}