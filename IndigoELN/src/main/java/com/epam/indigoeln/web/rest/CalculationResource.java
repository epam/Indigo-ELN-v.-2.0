package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.core.service.calculation.StoicCalculationService;
import com.epam.indigoeln.web.rest.dto.calculation.BasicBatchModel;
import com.epam.indigoeln.web.rest.dto.calculation.ProductTableDTO;
import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;
import com.epam.indigoeln.web.rest.dto.calculation.StoicTableDTO;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api
@RestController
@RequestMapping("/api/calculations")
public class CalculationResource {

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private StoicCalculationService stoicCalculationService;

    /**
     * PUT /molecule/info/ -> get calculated molecular fields
     */
    @ApiOperation(value = "Gets calculated molecular fields.", produces = "application/json")
    @RequestMapping(value = "/molecule/info",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> getMolecularInformation(
            @ApiParam("Molecule") @RequestBody(required = false) String molecule,
            @ApiParam("Salt Code") @RequestParam(required = false) String saltCode,
            @ApiParam("Salt Eq") @RequestParam(required = false) Float saltEq) {
        if (Strings.isNullOrEmpty(molecule)) {
            return ResponseEntity.ok(new HashMap<>());

        }
        return ResponseEntity.ok(calculationService.getMolecularInformation(normalizeMolFile(molecule),
                Optional.ofNullable(saltCode), Optional.ofNullable(saltEq)));
    }

    /**
     * PUT /molecule/chiral/ -> get is molecule chiral
     */
    @ApiOperation(value = "Checks if molecule is chiral.", produces = "application/json")
    @RequestMapping(value = "/molecule/chiral",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isMoleculeChiral(
            @ApiParam("Molecule") @RequestBody String molecule
        ) {
        return ResponseEntity.ok(calculationService.isMoleculeChiral(normalizeMolFile(molecule)));
    }

    /**
     * PUT /molecule/empty/ -> get is molecule empty
     */
    @ApiOperation(value = "Checks if molecule is empty.", produces = "application/json")
    @RequestMapping(value = "/molecule/empty",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Boolean> isEmptyMolecule(
            @ApiParam("Molecule") @RequestBody String molecule
        ) {
        return ResponseEntity.ok(calculationService.isMoleculeEmpty(normalizeMolFile(molecule)));
    }

    @ApiOperation(value = "Checks that all molecules have equal structure.", produces = "application/json")
    /**
     * PUT /molecule/equals/ -> check, that all molecules has equal structure
     */
    @RequestMapping(value = "/molecule/equals",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> moleculesEquals(
            @ApiParam("Molecules") @RequestBody List<String> molecules
        ) {
        return ResponseEntity.ok(calculationService.chemistryEquals(molecules, false));
    }

    /**
     * PUT /reaction/extract/ -> extract reaction components (structure, reactants and products)
     */
    @ApiOperation(value = "Extracts reaction components (structure, reactants and products).", produces = "application/json")
    @RequestMapping(value = "/reaction/extract",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReactionPropertiesDTO> extractReactionComponents(
            @ApiParam("Reaction") @RequestBody String reaction
        ) {
        return ResponseEntity.ok(calculationService.extractReactionComponents(normalizeMolFile(reaction)));
    }

    /**
     * PUT /reaction/combine/ -> combine reaction components (structure, reactants and products)
     */
    @ApiOperation(value = "Combine reaction components (structure, reactants and products.", produces = "application/json")
    @RequestMapping(value = "/reaction/combine",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReactionPropertiesDTO> combineReactionComponents(
            @ApiParam("Reaction data") @RequestBody ReactionPropertiesDTO reaction
        ) {
        return ResponseEntity.ok(calculationService.combineReactionComponents(reaction));
    }

    /**
     * PUT /reaction/equals/ -> check, that all reactions has equal structure
     */
    @ApiOperation(value = "Checks that all reactions have equal structure.", produces = "application/json")
    @RequestMapping(value = "/reaction/equals",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> reactionsEquals(
            @ApiParam("Reactions") @RequestBody List<String> reactions
        ) {
        return ResponseEntity.ok(calculationService.chemistryEquals(reactions, true));
    }

    /**
     * PUT /reaction/valid/ -> check, that reaction structure is valid
     */
    @ApiOperation(value = "Checks that reaction structure is valid.", produces = "application/json")
    @RequestMapping(value = "/reaction/valid",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isValidReaction(
            @ApiParam("Reaction") @RequestBody String reaction
        ) {
        return ResponseEntity.ok(calculationService.isValidReaction(normalizeMolFile(reaction)));
    }

    private String normalizeMolFile(String structure){
        return StringUtils.replace(structure, "\\n", System.getProperty("line.separator"));
    }

    /**
     * PUT /stoich/calculate -> calcalate stoich table
     */
    @RequestMapping(value = "/stoich/calculate",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StoicTableDTO> calculateStoicTable(@RequestBody StoicTableDTO stoicTableDTO) {
        return ResponseEntity.ok(stoicCalculationService.calculateStoicTable(stoicTableDTO));
    }

    /**
     * PUT /stoich/calculate -> calcalate stoich table based on batch
     */
    @RequestMapping(value = "/stoich/calculate/batch",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StoicTableDTO> calculateStoicTableBasedOnBatch(@RequestBody StoicTableDTO stoicTableDTO) {
        return ResponseEntity.ok(stoicCalculationService.calculateStoicTableBasedOnBatch(stoicTableDTO));
    }

    /**
     * PUT /product/calculate/batch -> calcalate batch from product batch summary table
     */
    @RequestMapping(value = "/product/calculate/batch",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BasicBatchModel> calculateProductBatch(@RequestBody ProductTableDTO productTableDTO) {
        return ResponseEntity.ok(stoicCalculationService.calculateProductBatch(productTableDTO));
    }

    /**
     * PUT /product/calculate/batch/amounts -> calcalate batch batch amounts
     */
    @RequestMapping(value = "/product/calculate/batch/amounts",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BasicBatchModel> recalculateBatchAmounts(@RequestBody ProductTableDTO productTableDTO) {
        return ResponseEntity.ok(stoicCalculationService.recalculateBatchAmounts(productTableDTO));
    }

}
