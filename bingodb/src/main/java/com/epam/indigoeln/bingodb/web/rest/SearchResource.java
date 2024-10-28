/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of BingoDB.
 *
 *  BingoDB is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BingoDB is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with BingoDB.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.bingodb.web.rest;

import com.epam.indigoeln.bingodb.service.BingoService;
import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for search operations.
 */
@RestController
@RequestMapping("api/search")
public class SearchResource {

    /**
     * Service instance for work with structure databases.
     */
    private final BingoService bingoService;

    /**
     * Create a new SearchResource instance.
     *
     * @param bingoService service instance for work with structure databases
     */
    @Autowired
    public SearchResource(BingoService bingoService) {
        this.bingoService = bingoService;
    }

    /**
     * Search molecules with exact match.
     *
     * @param structure molecule in Molfile/Smiles format
     * @param options   search options
     * @return REST response with OK status and found molecules with their IDs
     */
    @Operation(summary = "Search molecules with exact match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found molecules"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error occurred"
            )
    })
    @PostMapping(
            value = "molecule/exact",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchMoleculeExact(
            @Parameter(description = "Molecule in Molfile/Smiles format") @RequestBody String structure,
            @Parameter(description = "Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeExact(structure, options)));
    }

    /**
     * Search molecules with substructure match.
     *
     * @param structure molecule in Molfile/Smiles format
     * @param options   search options
     * @return REST response with OK status and found molecules with their IDs
     */
    @Operation(summary = "Search molecules with substructure match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found molecules"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error occurred"
            )
    })
    @PostMapping(
            value = "molecule/substructure",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchMoleculeSub(
            @Parameter(description = "Molecule in Molfile/Smiles format") @RequestBody String structure,
            @Parameter(description = "Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeSub(structure, options)));
    }

    /**
     * Search molecules with similarity match.
     *
     * @param structure molecule in Molfile/Smiles format
     * @param min       similarity min value
     * @param max       similarity max value
     * @param metric    similarity metric (default is 'tanimoto')
     * @return REST response with OK status and found molecules with their IDs
     */
    @Operation(summary = "Search molecules with similarity match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found molecules"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error occurred"
            )
    })
    @PostMapping(
            value = "molecule/similarity",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchMoleculeSimilarity(
            @Parameter(description = "Molecule in Molfile/Smiles format") @RequestBody String structure,
            @Parameter(description = "Similarity min") @RequestParam Float min,
            @Parameter(description = "Similarity max") @RequestParam Float max,
            @Parameter(description = "Similarity metric (default is 'tanimoto')") @RequestParam(required = false) String metric) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeSim(structure, min, max, metric)));
    }

    /**
     * Search molecules by molecular formula.
     *
     * @param molFormula molecular formula
     * @param options    search options
     * @return REST response with OK status and found molecules with their IDs
     */
    @Operation(summary = "Search molecules by molecular formula")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found molecules"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error occurred"
            )
    })
    @PostMapping(
            value = "molecule/molformula",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchMoleculeMolFormula(
            @Parameter(description = "Molecular formula") @RequestBody String molFormula,
            @Parameter(description = "Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeMolFormula(molFormula, options)));
    }

    /**
     * Search reactions with exact match.
     *
     * @param structure molecule or reaction in Molfile/Rxnfile/Smiles format
     * @param options   search options
     * @return REST response with OK status and found reactions with their IDs
     */
    @Operation(summary = "Search reactions with exact match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found reactions"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error occurred"
            )
    })
    @PostMapping(
            value = "reaction/exact",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchReactionExact(
            @Parameter(description = "Molecule or reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure,
            @Parameter(description = "Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionExact(structure, options)));
    }

    /**
     * Search reactions with substructure match.
     *
     * @param structure molecule or reaction in Molfile/Rxnfile/Smiles format
     * @param options   search options
     * @return REST response with OK status and found reactions with their IDs
     */
    @Operation(summary = "Search reactions with substructure match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found reactions"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error occurred"
            )
    })
    @PostMapping(
            value = "reaction/substructure",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchReactionSub(
            @Parameter(description = "Molecule or reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure,
            @Parameter(description = "Search options") @RequestParam(required = false) String options) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionSub(structure, options)));
    }

    /**
     * Search reactions with similarity match.
     *
     * @param structure molecule or reaction in Molfile/Rxnfile/Smiles format
     * @param min       similarity min
     * @param max       similarity max
     * @param metric    similarity metric (default is 'tanimoto')
     * @return REST response with OK status and found reactions with their IDs
     */
    @Operation(summary = "Search reactions with similarity match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found reactions"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error occurred"
            )
    })
    @PostMapping(
            value = "reaction/similarity",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> searchReactionSimilarity(
            @Parameter(description = "Molecule or reaction in Molfile/Rxnfile/Smiles format") @RequestBody String structure,
            @Parameter(description = "Similarity min") @RequestParam Float min,
            @Parameter(description = "Similarity max") @RequestParam Float max,
            @Parameter(description = "Similarity metric (default is 'tanimoto')") @RequestParam(required = false) String metric) {
        return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionSim(structure, min, max, metric)));
    }
}
