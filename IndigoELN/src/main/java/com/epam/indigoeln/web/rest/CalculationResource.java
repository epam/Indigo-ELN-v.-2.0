package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/calculations")
public class CalculationResource {

    @Autowired
    private CalculationService calculationService;

    /**
     * PUT /molecule/info/ -> get calculated molecular fields
     */
    @RequestMapping(value = "/molecule/info",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map> getMolecularInformation(@RequestBody(required = false) String molecule,
                                                       @RequestParam(required = false) String saltCode,
                                                       @RequestParam(required = false) Float saltEq) {
        if (Strings.isNullOrEmpty(molecule)) {
            return ResponseEntity.ok(new HashMap<>());

        }
        return ResponseEntity.ok(calculationService.getMolecularInformation(normalizeMolFile(molecule),
                Optional.ofNullable(saltCode), Optional.ofNullable(saltEq)));
    }

    /**
     * PUT /molecule/chiral/ -> get is molecule chiral
     */
    @RequestMapping(value = "/molecule/chiral",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isMoleculeChiral(@RequestBody String molecule) {
        return ResponseEntity.ok(calculationService.isMoleculeChiral(normalizeMolFile(molecule)));
    }

    /**
     * PUT /molecule/empty/ -> get is molecule empty
     */
    @RequestMapping(value = "/molecule/empty",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Boolean> isEmptyMolecule(@RequestBody String molecule) {
        return ResponseEntity.ok(calculationService.isMoleculeEmpty(normalizeMolFile(molecule)));
    }

    /**
     * PUT /molecule/equals/ -> check, that all molecules has equal structure
     */
    @RequestMapping(value = "/molecule/equals",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> moleculesEquals(@RequestBody List<String> molecules) {
        return ResponseEntity.ok(calculationService.chemistryEquals(molecules, false));
    }

    /**
     * PUT /reaction/extract/ -> extract reaction components (structure, reactants and products)
     */
    @RequestMapping(value = "/reaction/extract",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReactionPropertiesDTO> extractReactionComponents(@RequestBody String reaction) {
        return ResponseEntity.ok(calculationService.extractReactionComponents(normalizeMolFile(reaction)));
    }

    /**
     * PUT /reaction/combine/ -> combine reaction components (structure, reactants and products)
     */
    @RequestMapping(value = "/reaction/combine",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReactionPropertiesDTO> combineReactionComponents(@RequestBody ReactionPropertiesDTO reaction) {
        return ResponseEntity.ok(calculationService.combineReactionComponents(reaction));
    }

    /**
     * PUT /reaction/equals/ -> check, that all reactions has equal structure
     */
    @RequestMapping(value = "/reaction/equals",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> reactionsEquals(@RequestBody List<String> reactions) {
        return ResponseEntity.ok(calculationService.chemistryEquals(reactions, true));
    }

    /**
     * PUT /reaction/valid/ -> check, that reaction structure is valid
     */
    @RequestMapping(value = "/reaction/valid",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isValidReaction(@RequestBody String reaction) {
        return ResponseEntity.ok(calculationService.isValidReaction(normalizeMolFile(reaction)));
    }

    private String normalizeMolFile(String structure){
        return StringUtils.replace(structure, "\\n", System.getProperty("line.separator"));
    }
}
