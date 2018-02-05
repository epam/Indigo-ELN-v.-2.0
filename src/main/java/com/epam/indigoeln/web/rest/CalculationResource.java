package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;
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

    /**
     * PUT /molecule/info/ -> get calculated molecular fields.
     *
     * @param molecule Molecule
     * @param saltCode Salt Code
     * @param saltEq   Salt Eq
     * @return Returns calculated molecular fields
     */
    @ApiOperation(value = "Gets calculated molecular fields.")
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
     * GET  /api/calculations/salt_code_table?tableName=GCM_SALT_CDT ->
     * Returns all data from csv file salt information.
     *
     * @param tableName Table's name
     * @return Data from {@code tableName}
     */
    @ApiOperation(value = "Returns all data from csv file.")
    @RequestMapping(value = "salt_code_table", method = RequestMethod.GET)
    public List<Map<String, String>> getAllSaltCode(@RequestParam String tableName) {
        return calculationService.getCodeTableRows(tableName);
    }

    /**
     * PUT /molecule/chiral/ -> get is molecule chiral.
     *
     * @param molecule Molecule
     * @return Return true is molecule is chiral
     */
    @ApiOperation(value = "Checks if molecule is chiral.")
    @RequestMapping(value = "/molecule/chiral",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isMoleculeChiral(
            @ApiParam("Molecule") @RequestBody String molecule
    ) {
        return ResponseEntity.ok(calculationService.isMoleculeChiral(normalizeMolFile(molecule)));
    }

    /**
     * PUT /molecule/empty/ -> get is molecule empty.
     *
     * @param molecule Molecule
     * @return Returns true if molecule is empty
     */
    @ApiOperation(value = "Checks if molecule is empty.")
    @RequestMapping(value = "/molecule/empty",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Boolean> isEmptyMolecule(
            @ApiParam("Molecule") @RequestBody String molecule
    ) {
        return ResponseEntity.ok(calculationService.isMoleculeEmpty(normalizeMolFile(molecule)));
    }

    /**
     * PUT /molecule/equals/ -> check, that all molecules has equal structure.
     *
     * @param molecules Molecules
     * @return Returns true if molecules have equal structure
     */
    @ApiOperation(value = "Checks that all molecules have equal structure.")
    @RequestMapping(value = "/molecule/equals",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> moleculesEquals(
            @ApiParam("Molecules") @RequestBody List<String> molecules
    ) {
        return ResponseEntity.ok(calculationService.chemistryEquals(molecules));
    }

    /**
     * PUT /molecule/contains/ -> check, that the first molecule contains the second.
     *
     * @param molecules Molecules
     * @return Returns true if the first molecule contains the second
     */
    @ApiOperation(value = "Checks that the first molecule contains the second.")
    @RequestMapping(value = "/molecule/contains",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> moleculesSubstructure(
            @ApiParam("Molecules") @RequestBody List<String> molecules
    ) {
        if (molecules.size() < 2) {
            throw new IndigoRuntimeException("Please specify two molecules");
        }
        return ResponseEntity.ok(calculationService.chemistryContains(molecules.get(0), molecules.get(1)));
    }

    /**
     * PUT /reaction/extract/ -> extract reaction components (structure, reactants and products).
     *
     * @param reaction Reaction
     * @return Extracted reaction components
     */
    @ApiOperation(value = "Extracts reaction components (structure, reactants and products).")
    @RequestMapping(value = "/reaction/extract",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReactionPropertiesDTO> extractReactionComponents(
            @ApiParam("Reaction") @RequestBody String reaction
    ) {
        return ResponseEntity.ok(calculationService.extractReactionComponents(normalizeMolFile(reaction)));
    }

    /**
     * PUT /reaction/combine/ -> combine reaction components (structure, reactants and products).
     *
     * @param reaction Reaction data
     * @return Reaction properties
     */
    @ApiOperation(value = "Combine reaction components (structure, reactants and products.")
    @RequestMapping(value = "/reaction/combine",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReactionPropertiesDTO> combineReactionComponents(
            @ApiParam("Reaction data") @RequestBody ReactionPropertiesDTO reaction
    ) {
        return ResponseEntity.ok(calculationService.combineReactionComponents(reaction));
    }

    /**
     * PUT /reaction/equals/ -> check, that all reactions has equal structure.
     *
     * @param reactions Reactions
     * @return Returns true if all reactions have equal structure
     */
    @ApiOperation(value = "Checks that all reactions have equal structure.")
    @RequestMapping(value = "/reaction/equals",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> reactionsEquals(
            @ApiParam("Reactions") @RequestBody List<String> reactions
    ) {
        return ResponseEntity.ok(calculationService.chemistryEquals(reactions));
    }

    /**
     * PUT /reaction/valid/ -> check, that reaction structure is valid.
     *
     * @param reaction Reaction
     * @return Returns true if reaction structure is valid
     */
    @ApiOperation(value = "Checks that reaction structure is valid.")
    @RequestMapping(value = "/reaction/valid",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isValidReaction(
            @ApiParam("Reaction") @RequestBody String reaction
    ) {
        return ResponseEntity.ok(calculationService.isValidReaction(normalizeMolFile(reaction)));
    }

    private String normalizeMolFile(String structure) {
        return StringUtils.replace(structure, "\\n", System.getProperty("line.separator"));
    }
}
