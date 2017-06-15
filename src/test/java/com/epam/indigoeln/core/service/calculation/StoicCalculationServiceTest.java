package com.epam.indigoeln.core.service.calculation;

import com.epam.indigoeln.core.util.EqualsUtil;
import com.epam.indigoeln.web.rest.dto.calculation.BasicBatchModel;
import com.epam.indigoeln.web.rest.dto.calculation.ProductTableDTO;
import com.epam.indigoeln.web.rest.dto.calculation.StoicTableDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.ScalarValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.StringValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.UnitValueDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;


public class StoicCalculationServiceTest {

    private StoicCalculationService stoicCalculationService;
    private static final double REC_MW_1 = 58.12;
    private static final double REC_MW_2 = 44.096;
    private static final double REC_MW_3 = 78.112;
    private static final double PROD_MW = 176.298;
    private static final double WEIGHT = 15;
    private static final double TOTAL_WEIGHT = 40;

    @Before
    public void setUp(){
        stoicCalculationService = new StoicCalculationService();
    }

    @Test
    public void calculateStoicTable(){
        StoicTableDTO stoicTableDTO = getStoicTableDTO();
        assertEquals(false,stoicTableDTO.getStoicBatches().get(0).isLimiting());
        assertEquals(false,stoicTableDTO.getStoicBatches().get(1).isLimiting());
        assertEquals(false,stoicTableDTO.getStoicBatches().get(2).isLimiting());

        StoicTableDTO result = stoicCalculationService.calculateStoicTable(stoicTableDTO);
        assertEquals(true,result.getStoicBatches().get(0).isLimiting());
        assertEquals(false,result.getStoicBatches().get(1).isLimiting());
        assertEquals(false,result.getStoicBatches().get(2).isLimiting());
    }

    @Test
    public void calculateStoicTableBasedOnBatch(){
        StoicTableDTO stoicTableDTO = getStoicTableDTO();

        stoicTableDTO.getStoicBatches().get(0).setLimiting(true);
        stoicTableDTO.getStoicBatches().get(0).getWeight().setValue(WEIGHT);
        stoicTableDTO.setChangedBatchRowNumber(0);
        stoicTableDTO.setChangedField("weight");

        double expectedMol = WEIGHT / stoicTableDTO.getStoicBatches().get(0).getMolWeight().getValue();

        double actualMol1 = stoicTableDTO.getStoicBatches().get(0).getMol().getValue();
        double actualMol2 = stoicTableDTO.getStoicBatches().get(1).getMol().getValue();
        double actualMol3 = stoicTableDTO.getStoicBatches().get(2).getMol().getValue();
        double actualProdMol = stoicTableDTO.getIntendedProducts().get(0).getTheoMoles().getValue();

        assertNotEquals(expectedMol, actualMol1, EqualsUtil.PRECISION);
        assertNotEquals(expectedMol, actualMol2, EqualsUtil.PRECISION);
        assertNotEquals(expectedMol, actualMol3, EqualsUtil.PRECISION);
        assertNotEquals(expectedMol, actualProdMol, EqualsUtil.PRECISION);

        StoicTableDTO result = stoicCalculationService.calculateStoicTableBasedOnBatch(stoicTableDTO);

        actualMol1 = result.getStoicBatches().get(0).getMol().getValue();
        actualMol2 = result.getStoicBatches().get(1).getMol().getValue();
        actualMol3 = result.getStoicBatches().get(2).getMol().getValue();
        actualProdMol = result.getIntendedProducts().get(0).getTheoMoles().getValue();

        assertEquals(expectedMol, actualMol1, EqualsUtil.PRECISION);
        assertEquals(expectedMol, actualMol2, EqualsUtil.PRECISION);
        assertEquals(expectedMol, actualMol3, EqualsUtil.PRECISION);
        assertEquals(expectedMol, actualProdMol, EqualsUtil.PRECISION);

        double expectedWeight2 = REC_MW_2 * expectedMol;
        double expectedWeight3 = REC_MW_3 * expectedMol;
        double expectedTheoWeight = PROD_MW * expectedMol;

        double actualWeight2 = result.getStoicBatches().get(1).getWeight().getValue();
        double actualWeight3 = result.getStoicBatches().get(2).getWeight().getValue();
        double actualTheoWeight = result.getIntendedProducts().get(0).getWeight().getValue();

        assertEquals(expectedWeight2, actualWeight2,EqualsUtil.PRECISION);
        assertEquals(expectedWeight3, actualWeight3,EqualsUtil.PRECISION);
        assertEquals(expectedTheoWeight, actualTheoWeight,EqualsUtil.PRECISION);
    }

    @Test
    public void calculateProductBatch(){
        ProductTableDTO productTableDTO = getProductTableDTO();
        BasicBatchModel productBatch = productTableDTO.getProductBatch();

        double expectedTotalMol = productBatch.getTotalWeight().getValue() / productBatch.getMolWeight().getValue();
        double expectedYield =  expectedTotalMol / productBatch.getTheoMoles().getValue() * 100;

        assertNotEquals(expectedTotalMol,productBatch.getTotalMoles().getValue(), EqualsUtil.PRECISION);
        assertNotEquals(expectedYield,productBatch.getYield(), EqualsUtil.PRECISION);

        BasicBatchModel result = stoicCalculationService.calculateProductBatch(productTableDTO);

        assertEquals(expectedTotalMol,result.getMol().getValue(), EqualsUtil.PRECISION);
        assertEquals(expectedYield,result.getYield(), EqualsUtil.PRECISION);
    }

    private StoicTableDTO getStoicTableDTO(){
        BasicBatchModel reactant1 = new BasicBatchModel();
        reactant1.setMolWeight(new ScalarValueDTO(REC_MW_1, null, false, false));
        reactant1.setMol(new UnitValueDTO(0, null, "mmol", false, false));
        reactant1.setWeight(new UnitValueDTO(0, null, "mg", false, false));
        reactant1.setVolume(new UnitValueDTO(0, null, "mL", false, false));
        reactant1.setDensity(new UnitValueDTO(0, null, "g/mL", false, false));
        reactant1.setMolarity(new UnitValueDTO(0, null, "M", false, false));
        reactant1.setStoicPurity(new ScalarValueDTO(100, null, false, false));
        reactant1.setEq(new ScalarValueDTO(1, null, false, false));
        reactant1.setLimiting(false);
        reactant1.setRxnRole(new StringValueDTO("REACTANT", "REACTANT", false, false));
        reactant1.setTotalVolume(new UnitValueDTO(0, null, "mL", false, false));
        reactant1.setTotalWeight(new UnitValueDTO(0, null, "mg", false, false));
        reactant1.setTotalMoles(new UnitValueDTO(0, null, "mmol", false, false));
        reactant1.setLoadFactor(new UnitValueDTO(0, null, "", false, false));
        reactant1.setLastUpdatedType("mol");
        reactant1.setYield(0);
        reactant1.setPrevMolarAmount(new UnitValueDTO(0, null, "M", false, false));
        reactant1.setTheoMoles(new UnitValueDTO(0, null, "mmol", false, false));
        reactant1.setTheoWeight(new UnitValueDTO(0, null, "mg", false, false));

        BasicBatchModel reactant2 = new BasicBatchModel();
        reactant2.setMolWeight(new ScalarValueDTO(REC_MW_2, null, false, false));
        reactant2.setMol(new UnitValueDTO(0, null, "mmol", false, false));
        reactant2.setWeight(new UnitValueDTO(0, null, "mg", false, false));
        reactant2.setVolume(new UnitValueDTO(0, null, "mL", false, false));
        reactant2.setDensity(new UnitValueDTO(0, null, "g/mL", false, false));
        reactant2.setMolarity(new UnitValueDTO(0, null, "M", false, false));
        reactant2.setStoicPurity(new ScalarValueDTO(100, null, false, false));
        reactant2.setEq(new ScalarValueDTO(1, null, false, false));
        reactant2.setLimiting(false);
        reactant2.setRxnRole(new StringValueDTO("REACTANT", "REACTANT", false, false));
        reactant2.setTotalVolume(new UnitValueDTO(0, null, "mL", false, false));
        reactant2.setTotalWeight(new UnitValueDTO(0, null, "mg", false, false));
        reactant2.setTotalMoles(new UnitValueDTO(0, null, "mmol", false, false));
        reactant2.setLoadFactor(new UnitValueDTO(0, null, "", false, false));
        reactant2.setLastUpdatedType("mol");
        reactant2.setYield(0);
        reactant2.setPrevMolarAmount(new UnitValueDTO(0, null, "M", false, false));
        reactant2.setTheoMoles(new UnitValueDTO(0, null, "mmol", false, false));
        reactant2.setTheoWeight(new UnitValueDTO(0, null, "mg", false, false));

        BasicBatchModel reactant3 = new BasicBatchModel();
        reactant3.setMolWeight(new ScalarValueDTO(REC_MW_3, null, false, false));
        reactant3.setMol(new UnitValueDTO(0, null, "mmol", false, false));
        reactant3.setWeight(new UnitValueDTO(0, null, "mg", false, false));
        reactant3.setVolume(new UnitValueDTO(0, null, "mL", false, false));
        reactant3.setDensity(new UnitValueDTO(0, null, "g/mL", false, false));
        reactant3.setMolarity(new UnitValueDTO(0, null, "M", false, false));
        reactant3.setStoicPurity(new ScalarValueDTO(100, null, false, false));
        reactant3.setEq(new ScalarValueDTO(1, null, false, false));
        reactant3.setLimiting(false);
        reactant3.setRxnRole(new StringValueDTO("REACTANT", "REACTANT", false, false));
        reactant3.setTotalVolume(new UnitValueDTO(0, null, "mL", false, false));
        reactant3.setTotalWeight(new UnitValueDTO(0, null, "mg", false, false));
        reactant3.setTotalMoles(new UnitValueDTO(0, null, "mmol", false, false));
        reactant3.setLoadFactor(new UnitValueDTO(0, null, "", false, false));
        reactant3.setLastUpdatedType("mol");
        reactant3.setYield(0);
        reactant3.setPrevMolarAmount(new UnitValueDTO(0, null, "M", false, false));
        reactant3.setTheoMoles(new UnitValueDTO(0, null, "mmol", false, false));
        reactant3.setTheoWeight(new UnitValueDTO(0, null, "mg", false, false));

        BasicBatchModel intendedProduct = new BasicBatchModel();
        intendedProduct.setMolWeight(new ScalarValueDTO(PROD_MW, null, false, false));
        intendedProduct.setMol(new UnitValueDTO(0, null, "mmol", false, false));
        intendedProduct.setWeight(new UnitValueDTO(0, null, "mg", false, false));
        intendedProduct.setVolume(new UnitValueDTO(0, null, "mL", false, false));
        intendedProduct.setDensity(new UnitValueDTO(0, null, "g/mL", false, false));
        intendedProduct.setMolarity(new UnitValueDTO(0, null, "M", false, false));
        intendedProduct.setStoicPurity(new ScalarValueDTO(100, null, false, false));
        intendedProduct.setEq(new ScalarValueDTO(1, null, false, false));
        intendedProduct.setLoadFactor(new UnitValueDTO(0, null, "", false, false));
        intendedProduct.setLimiting(false);
        intendedProduct.setRxnRole(new StringValueDTO("REACTANT", "REACTANT", false, false));
        intendedProduct.setTotalVolume(new UnitValueDTO(0, null, "mL", false, false));
        intendedProduct.setTotalWeight(new UnitValueDTO(0, null, "mg", false, false));
        intendedProduct.setTotalMoles(new UnitValueDTO(0, null, "mmol", false, false));
        intendedProduct.setYield(0);
        intendedProduct.setTheoWeight(new UnitValueDTO(0, null, "mg", false, false));
        intendedProduct.setTheoMoles(new UnitValueDTO(0, null, "mmol", false, false));
        intendedProduct.setLastUpdatedType("mol");
        intendedProduct.setPrevMolarAmount(new UnitValueDTO(0, null, "M", false, false));

        StoicTableDTO stoicTableDTO = new StoicTableDTO();
        stoicTableDTO.setStoicBatches(Arrays.asList(reactant1, reactant2, reactant3));
        stoicTableDTO.setIntendedProducts(Arrays.asList(intendedProduct));
        stoicTableDTO.setActualProducts(new ArrayList<>());
        return stoicTableDTO;
    }

    private ProductTableDTO getProductTableDTO(){
        double mol = WEIGHT / REC_MW_1;
        double weight = PROD_MW * mol;

        BasicBatchModel product = new BasicBatchModel();
        product.setMolWeight(new ScalarValueDTO(PROD_MW, null, false, false));
        product.setMol(new UnitValueDTO(0, null, "mmol", false, false));
        product.setWeight(new UnitValueDTO(weight, null, "mg", false, false));
        product.setVolume(new UnitValueDTO(0, null, "mL", false, false));
        product.setDensity(new UnitValueDTO(0, null, "g/mL", false, false));
        product.setMolarity(new UnitValueDTO(0, null, "M", false, false));
        product.setStoicPurity(new ScalarValueDTO(100, null, false, false));
        product.setEq(new ScalarValueDTO(1, null, false, false));
        product.setLoadFactor(new UnitValueDTO(0, null, "", false, false));
        product.setLimiting(false);
        product.setRxnRole(new StringValueDTO("REACTANT", "REACTANT", false, false));
        product.setTotalVolume(new UnitValueDTO(0, null, "mL", false, false));
        product.setTotalWeight(new UnitValueDTO(TOTAL_WEIGHT, null, "mg", false, false));
        product.setTotalMoles(new UnitValueDTO(0, null, "mmol", false, false));
        product.setYield(0);
        product.setTheoWeight(new UnitValueDTO(weight, null, "mg", false, false));
        product.setTheoMoles(new UnitValueDTO(mol, null, "mmol", false, false));
        product.setLastUpdatedType("mol");
        product.setPrevMolarAmount(new UnitValueDTO(0, null, "M", false, false));

        ProductTableDTO productTableDTO = new ProductTableDTO();
        productTableDTO.setProductBatch(product);
        productTableDTO.setChangedField("totalWeight");
        return productTableDTO;
    }
}