package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.core.service.calculation.helper.RendererResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/renderer")
public class StructureRendererResource {

    @Autowired
    private CalculationService calculationService;

    /**
     * POST /molecule/image -> get molecule's graphical representation
     */
    @RequestMapping(value = "/{type}/image", method = RequestMethod.POST)
    public ResponseEntity<RendererResult> getMoleculeImagePOST(@PathVariable String type, @RequestBody String structure) {
        RendererResult result = calculationService.getStructureWithImage(structure, type);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
