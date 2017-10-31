package com.epam.indigoeln.bingodb.web.rest;

import com.epam.indigoeln.bingodb.service.BingoService;
import com.epam.indigoeln.bingodb.web.rest.dto.ErrorDTO;
import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api/search")
public class SearchResource {

    private final BingoService bingoService;

    @Autowired
    public SearchResource(BingoService bingoService) {
        this.bingoService = bingoService;
    }

    @ApiOperation("Search molecules with exact match")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found molecules"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PostMapping(value = "molecule/exact", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchMoleculeExact(@ApiParam("Molecule in Molfile/Smiles format") @RequestBody String structure,
                                                           @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeExact(structure, options)));
    }

    @ApiOperation("Search molecules with substructure match")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found molecules"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PostMapping(value = "molecule/substructure", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchMoleculeSub(@ApiParam("Molecule in Molfile/Smiles format") @RequestBody String structure,
                                                         @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeSub(structure, options)));
    }

    @ApiOperation("Search molecules with similarity match")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found molecules"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PostMapping(value = "molecule/similarity", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchMoleculeSimilarity(@ApiParam("Molecule in Molfile/Smiles format") @RequestBody String structure,
                                                                @ApiParam("Similarity min") @RequestParam Float min,
                                                                @ApiParam("Similarity max") @RequestParam Float max,
                                                                @ApiParam("Similarity metric (default is 'tanimoto')") @RequestParam(required = false) String metric) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeSim(structure, min, max, metric)));
    }

    @ApiOperation("Search molecules by molecular formula")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found molecules"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PostMapping(value = "molecule/molformula", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchMoleculeMolFormula(@ApiParam("Molecular formula") @RequestBody String molFormula,
                                                                @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeMolFormula(molFormula, options)));
    }

    @ApiOperation("Search reactions with exact match")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found reactions"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PostMapping(value = "reaction/exact", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchReactionExact(@ApiParam("Molecule or Reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure,
                                                           @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionExact(structure, options)));
    }

    @ApiOperation("Search reactions with substructure match")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found reactions"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PostMapping(value = "reaction/substructure", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchReactionSub(@ApiParam("Molecule or Reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure,
                                                         @ApiParam("Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionSub(structure, options)));
    }

    @ApiOperation("Search reactions with similarity match")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found reactions"),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "Server error occurred", response = ErrorDTO.class)
    })
    @PostMapping(value = "reaction/similarity", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchReactionSimilarity(@ApiParam("Molecule or Reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure,
                                                                @ApiParam("Similarity min") @RequestParam Float min,
                                                                @ApiParam("Similarity max") @RequestParam Float max,
                                                                @ApiParam("Similarity metric (default is 'tanimoto')") @RequestParam(required = false) String metric) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionSim(structure, min, max, metric)));
    }
}
