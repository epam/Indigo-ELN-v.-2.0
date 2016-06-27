package com.chemistry.enotebook;

import com.chemistry.enotebook.domain.AmountModel;
import com.chemistry.enotebook.domain.BatchModel;
import com.chemistry.enotebook.domain.MonomerBatchModel;
import com.chemistry.enotebook.domain.ProductBatchModel;
import com.chemistry.enotebook.experiment.common.units.Unit2;
import com.epam.indigoeln.web.rest.dto.calculation.BasicBatchModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

import static com.chemistry.enotebook.experiment.common.units.UnitType.*;

public class ProductCalculator {

    private static final Log log = LogFactory.getLog(ProductCalculator.class);

    private static final int WEIGHT_AMOUNT = 1;
    private static final int VOLUME_AMOUNT = 2;
    private static final int MOLES_AMOUNT = 3;

    private static void setTotalAmountMadeWeight(ProductBatchModel batch, AmountModel amount) {
        batch.setTotalWeightAmountQuitly(new AmountModel(MASS, 0));
        batch.setTotalWeightAmount(amount);
        batch.getTotalWeight().setCalculated(false);
        batch.recalcAmounts();
        //Sync all units
        ArrayList objectList = new ArrayList();
        syncUnitsAndSigDigits(objectList, amount, WEIGHT_AMOUNT);
    }

    private static void setTotalAmountMadeVolume(ProductBatchModel batch, AmountModel amount) {
        batch.setTotalVolumeAmountQuitly(new AmountModel(VOLUME, 0));
        batch.setTotalVolumeAmount(amount);
        batch.getTotalVolume().setCalculated(false);
        batch.recalcAmounts();
        //Sync all units
        ArrayList objectList = new ArrayList();
        syncUnitsAndSigDigits(objectList, amount, VOLUME_AMOUNT);
    }

    private static void setTotalAmountMadeMoles(BatchModel batch, AmountModel totalMolesAmountModel) {
        double totalWeightInStdUnits = totalMolesAmountModel.GetValueInStdUnitsAsDouble() * batch.getMolWgt();
        AmountModel newTotalWeightAmountModel = new AmountModel(MASS, totalWeightInStdUnits);
        newTotalWeightAmountModel.setUnit(batch.getTotalWeight().getUnit()); //Do not change unit based on moles. Total wt unit takes precedence.
        newTotalWeightAmountModel.setSigDigits(batch.getTotalWeight().getSigDigits());
        setTotalAmountMadeWeight((ProductBatchModel) batch, newTotalWeightAmountModel);
        batch.getTheoreticalMoleAmount().setUnit(totalMolesAmountModel.getUnit());
    }

    private static void syncUnitsAndSigDigits(ArrayList objectList, AmountModel amount, int property) {
        Unit2 unit = amount.getUnit();
        int sigDigits = amount.getSigDigits();
        for (Object object : objectList) {
            if (object == null)
                continue;
            switch (property) {
                case WEIGHT_AMOUNT:
                    if (object instanceof ProductBatchModel) {
                        ProductBatchModel productBatchModel = (ProductBatchModel) object;
                        productBatchModel.getTotalWeight().setUnit(unit);
                        productBatchModel.getTotalWeight().setSigDigits(sigDigits);
                        productBatchModel.getTheoreticalWeightAmount().setUnit(unit);
                        productBatchModel.getTheoreticalWeightAmount().setSigDigits(sigDigits);
                    } else if (object instanceof MonomerBatchModel) {
                        MonomerBatchModel monomerBatchModel = (MonomerBatchModel) object;
                        monomerBatchModel.getStoicWeightAmount().setUnit(unit);
                        monomerBatchModel.getStoicWeightAmount().setSigDigits(sigDigits);
                    }
                    break;

                case VOLUME_AMOUNT:
                    if (object instanceof ProductBatchModel) {
                        ProductBatchModel productBatchModel = (ProductBatchModel) object;
                        productBatchModel.getTotalVolume().setUnit(unit);
                        productBatchModel.getTotalVolume().setSigDigits(sigDigits);
                    }
                    break;

                case MOLES_AMOUNT:
                    if (object instanceof ProductBatchModel) {
                        //ProductBatchModel productBatchModel = (ProductBatchModel)object;
                        //productBatchModel.getTotalVolume().setUnit(unit);
                        //productBatchModel.getVolumeAmount().setUnit(unit);
                    } else if (object instanceof MonomerBatchModel) {
                        MonomerBatchModel monomerBatchModel = (MonomerBatchModel) object;
                        monomerBatchModel.getStoicMoleAmount().setUnit(unit);
                        monomerBatchModel.getStoicMoleAmount().setSigDigits(sigDigits);
                    }
                    break;
            }
        }
    }

    public void calculateProductBatch(ProductBatchModel batch, BasicBatchModel rawBatch, String changedField) {
        AmountModel amount;
        switch (changedField) {
            case "totalWeight":
                amount = new AmountModel(MASS, rawBatch.getTotalWeight().getValue(), !rawBatch.getTotalWeight().isEntered());
                setTotalAmountMadeWeight(batch, amount);
                break;
            case "totalVolume":
                amount = new AmountModel(VOLUME, rawBatch.getTotalVolume().getValue(), !rawBatch.getTotalVolume().isEntered());
                setTotalAmountMadeVolume(batch, amount);
                break;
            case "totalMoles":
                amount = new AmountModel(MOLES, rawBatch.getTotalMoles().getValue(), !rawBatch.getTheoMoles().isEntered());
                setTotalAmountMadeMoles(batch, amount);
                break;
            default:
                batch.recalcAmounts();
        }

    }

}
