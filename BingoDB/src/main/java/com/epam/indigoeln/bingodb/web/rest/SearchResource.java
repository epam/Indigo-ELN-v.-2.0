package com.epam.indigoeln.bingodb.web.rest;

import com.epam.indigoeln.bingodb.service.BingoService;
import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/api/search",
        consumes = MediaType.TEXT_PLAIN_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class SearchResource {

    private final BingoService bingoService;

    @Autowired
    public SearchResource(BingoService bingoService) {
        this.bingoService = bingoService;
    }

    @ApiOperation("Search molecule with exact match")
    @PostMapping("/molecule/exact")
    public ResponseEntity<ResponseDTO> searchMoleculeExact(@ApiParam("Molecule in Molfile/Smiles format") @RequestBody String s,
                                                           @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeExact(s, options)));
    }

    @ApiOperation("Search molecule with substructure match")
    @PostMapping("/molecule/substructure")
    public ResponseEntity<ResponseDTO> searchMoleculeSub(@ApiParam("Molecule in Molfile/Smiles format") @RequestBody String s,
                                                         @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeSub(s, options)));
    }

    @ApiOperation("Search molecule with similarity match")
    @PostMapping("/molecule/similarity")
    public ResponseEntity<ResponseDTO> searchMoleculeSimilarity(@ApiParam("Molecule in Molfile/Smiles format") @RequestBody String s,
                                                                @ApiParam("Similarity min") @RequestParam Float min,
                                                                @ApiParam("Similarity max") @RequestParam Float max,
                                                                @ApiParam("Similarity metric (default is 'tanimoto')") @RequestParam(required = false) String metric) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeSim(s, min, max, metric)));
    }

    @ApiOperation("Search molecule by molecular formula")
    @PostMapping("/molecule/molformula")
    public ResponseEntity<ResponseDTO> searchMoleculeMolFormula(@ApiParam("Molecular formula") @RequestBody String molFormula,
                                                                @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeMolFormula(molFormula, options)));
    }

    @ApiOperation("Search reaction with exact match")
    @PostMapping("/reaction/exact")
    public ResponseEntity<ResponseDTO> searchReactionExact(@ApiParam("Reaction in Rxnfile/Smiles format") @RequestBody String s,
                                                           @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionExact(s, options)));
    }

    @ApiOperation("Search reaction with substructure match")
    @PostMapping("/reaction/substructure")
    public ResponseEntity<ResponseDTO> searchReactionSub(@ApiParam("Reaction in Rxnfile/Smiles format") @RequestBody String s,
                                                         @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionSub(s, options)));
    }

    @ApiOperation("Search reaction with similarity match")
    @PostMapping("/reaction/similarity")
    public ResponseEntity<ResponseDTO> searchReactionSimilarity(@ApiParam("Reaction in Rxnfile/Smiles format") @RequestBody String s,
                                                                @ApiParam("Similarity min") @RequestParam Float min,
                                                                @ApiParam("Similarity max") @RequestParam Float max,
                                                                @ApiParam("Similarity metric (default is 'tanimoto')") @RequestParam(required = false) String metric) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionSim(s, min, max, metric)));
    }
}
