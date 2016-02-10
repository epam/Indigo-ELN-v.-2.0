package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.bingodb.BingoDbIntegrationService;
import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.core.service.calculation.helper.RenderedStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/renderer")
public class StructureRendererResource {

    private static final String MOLECULE_TYPE = "molecule";
    private static final String REACTION_TYPE = "reaction";

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private BingoDbIntegrationService bingoDbIntegrationService;


    /**
     * GET /molecule/:id/image -> get structure of molecule and its graphical repr
     */
    @RequestMapping(value = "/molecule/{id}/image", method = RequestMethod.GET)
    public ResponseEntity<RenderedStructure> getMoleculeImage(@PathVariable Integer id,
                                                       @RequestParam(value = "width", required=false) Integer width,
                                                       @RequestParam(value = "height", required=false) Integer height) {

        String structure = bingoDbIntegrationService.getMolecule(id).get();
        RenderedStructure result = calculationService.getStructureWithImage(structure, MOLECULE_TYPE, width, height);

        return new ResponseEntity<RenderedStructure>(result, HttpStatus.OK);
    }

    /**
     * GET /reaction/:id/image -> get structure of molecule and its graphical repr
     */
    @RequestMapping(value = "/reaction/{id}/image", method = RequestMethod.GET)
    public ResponseEntity<RenderedStructure> getReactionImage(@PathVariable Integer id,
                                                       @RequestParam(value = "width", required=false) Integer width,
                                                       @RequestParam(value = "height", required=false) Integer height) {

        String structure = bingoDbIntegrationService.getReaction(id).get();
        RenderedStructure result = calculationService.getStructureWithImage(structure, REACTION_TYPE, width, height);

        return new ResponseEntity<RenderedStructure>(result, HttpStatus.OK);
    }

}
