package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.calculation.CalculationService;
import com.epam.indigoeln.core.service.calculation.helper.RendererResult;
import com.epam.indigoeln.web.rest.dto.rendering.RendererDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/renderer")
public class StructureRendererResource {

    private static final String MOLECULE_TYPE = "molecule";
    private static final String REACTION_TYPE = "reaction";

    @Autowired
    private CalculationService calculationService;

    /**
     * POST /molecule/image -> get molecule's graphical representation
     */
    @RequestMapping(value = "/molecule/image", method = RequestMethod.POST)
    public ResponseEntity<RendererResult> getMoleculeImagePOST(@RequestBody RendererDataDTO rendererData) {

        RendererResult result = calculationService.getStructureWithImage(rendererData.getStructure(),
                MOLECULE_TYPE, rendererData.getWidth(), rendererData.getHeight());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * POST /reaction/image -> get reaction's graphical representation
     */
    @RequestMapping(value = "/reaction/image", method = RequestMethod.POST)
    public ResponseEntity<RendererResult> getReactionImagePOST(@RequestBody RendererDataDTO rendererData) {

        RendererResult result = calculationService.getStructureWithImage(rendererData.getStructure(),
                REACTION_TYPE, rendererData.getWidth(), rendererData.getHeight());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
