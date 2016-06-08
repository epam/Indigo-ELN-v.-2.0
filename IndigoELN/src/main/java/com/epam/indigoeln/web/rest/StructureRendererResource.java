package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.core.service.calculation.helper.RendererResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api
@RestController
@RequestMapping("/api/renderer")
public class StructureRendererResource {

    @Autowired
    private CalculationService calculationService;

    /**
     * POST /molecule/image -> get molecule's graphical representation
     */
    @ApiOperation(value = "Returns molecule's graphical representation.", produces = "application/json")
    @RequestMapping(value = "/{type}/image", method = RequestMethod.POST)
    public ResponseEntity<RendererResult> getMoleculeImagePOST(
            @ApiParam("Representation type.") @PathVariable String type,
            @ApiParam("Molecule structure.") @RequestBody String structure
        ) {
        RendererResult result = calculationService.getStructureWithImage(structure, type);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
