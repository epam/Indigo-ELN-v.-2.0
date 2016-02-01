package com.epam.indigoeln.web.rest;

import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;
import com.epam.indigoeln.core.service.calculation.CalculationService;

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
    public ResponseEntity<Map> getMolecularInformation(@RequestBody String molecule) {
        return ResponseEntity.ok(calculationService.getMolecularInformation(normalizeMolfile(molecule)));
    }

    /**
     * PUT /molecule/chiral/ -> get is molecule chiral
     */
    @RequestMapping(value = "/molecule/chiral",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isMoleculeChiral(@RequestBody String molecule) {
        return ResponseEntity.ok(calculationService.isMoleculeChiral(normalizeMolfile(molecule)));
    }

    /**
     * PUT /molecule/empty/ -> get is molecule empty
     */
    @RequestMapping(value = "/molecule/empty",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Boolean> isEmptyMolecule(@RequestBody String molecule) {
        return ResponseEntity.ok(calculationService.isMoleculeEmpty(normalizeMolfile(molecule)));
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
        return ResponseEntity.ok(calculationService.extractReactionComponents(normalizeMolfile(reaction)));
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
        return ResponseEntity.ok(calculationService.isValidReaction(normalizeMolfile(reaction)));
    }

    private String normalizeMolfile(String structure){
        return StringUtils.replace(structure, "\\n", System.getProperty("line.separator"));
    }
}
