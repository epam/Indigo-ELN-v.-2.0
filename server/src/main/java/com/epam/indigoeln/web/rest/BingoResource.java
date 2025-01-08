/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.core.service.calculation.CalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bingodb")
public class BingoResource {

    private static final String BINGODB_MOLECULE_PATH = "/api/bingodb/molecule/";
    private static final String BINGODB_REACTION_PATH = "/api/bingodb/reaction/";

    @Autowired
    private BingoService bingoService;
    @Autowired
    private CalculationService calculationService;

    /**
     * GET /molecule/:id -> get structure of molecule.
     *
     * @param id Identifier of the molecule
     * @return Returns the structure of molecule
     */
    @Operation(summary = "Returns the structure of molecule.")
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMolecule(
            @Parameter(description = "Identifier of the molecule.") @PathVariable String id
    ) {
        return bingoService
                .getById(id)
                .map(molecule -> new ResponseEntity<>(molecule, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /molecule/ -> create new molecule.
     *
     * @param molecule The molecule to create
     * @return Created molecule
     * @throws URISyntaxException If URI is not correct
     */
    @Operation(summary = "Creates a new molecule.")
    @RequestMapping(value = "/molecule/", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> addMolecule(
            @Parameter(description = "The molecule to create.") @RequestBody String molecule
    ) throws URISyntaxException {
        Optional<String> result = bingoService.insert(molecule);

        if (result.isPresent()) {
            return ResponseEntity
                    .created(new URI(BINGODB_MOLECULE_PATH + result.get()))
                    .body(result.get());
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /molecule/:id -> update molecule.
     *
     * @param id       Id of the molecule to update
     * @param molecule New molecule data
     * @return Updated molecule
     */
    @Operation(summary = "Updates the molecule.")
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.PUT, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateMolecule(
            @Parameter(description = "Id of the molecule to update.") @PathVariable String id,
            @Parameter(description = "New molecule data.") @RequestBody String molecule
    ) {
        if (!bingoService.getById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<String> result = bingoService.update(id, molecule);

        if (result.isPresent()) {
            return ResponseEntity
                    .ok()
                    .body(result.get());
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /molecule/empty -> checks if molecule is empty.
     *
     * @param molecule Molecule
     * @return Returns true if molecule is empty
     */
    @Operation(summary = "Checks if molecule is empty.")
    @RequestMapping(value = "/molecule/empty", method = RequestMethod.POST)
    public ResponseEntity<Object> isEmptyMolecule(
            @Parameter(description = "Molecule to check.") @RequestBody String molecule) {
        Optional<Boolean> result = calculationService.isEmptyMolecule(molecule);

        if (result.isPresent()) {
            return ResponseEntity
                    .ok().body(Collections.singletonMap("empty", result.get()));
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /molecule/:id -> delete molecule.
     *
     * @param id Id of the molecule to delete
     */
    @Operation(summary = "Deletes the molecule.")
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "Id of the molecule to delete.") @PathVariable String id
    ) {
        bingoService.delete(id);
        return ResponseEntity
                .ok()
                .build();
    }

    /**
     * GET /reaction/:id -> get reaction structure.
     *
     * @param id Id of the reaction
     * @return Returns structure of the reaction
     */
    @Operation(summary = "Returns structure of the reaction.")
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getReaction(
            @Parameter(description = "Id of the reaction.") @PathVariable String id
    ) {
        return bingoService
                .getById(id)
                .map(molecule -> new ResponseEntity<>(molecule, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /reaction/ -> create new reaction.
     *
     * @param reaction Reaction to create
     * @return Created reaction
     * @throws URISyntaxException If URI is not correct
     */
    @Operation(summary = "Creates new reaction.")
    @RequestMapping(value = "/reaction/", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> addReaction(
            @Parameter(description = "Reaction to create.") @RequestBody String reaction
    ) throws URISyntaxException {
        Optional<String> result = bingoService.insert(reaction);

        if (result.isPresent()) {
            return ResponseEntity
                    .created(new URI(BINGODB_REACTION_PATH + result.get()))
                    .body(result.get());
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /reaction/:id -> update existing reaction.
     *
     * @param id       Id of the reaction to update
     * @param reaction New reaction data
     * @return Updated reaction
     */
    @Operation(summary = "Updates the reaction.")
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.PUT, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateReaction(
            @Parameter(description = "Id of the reaction to update.") @PathVariable String id,
            @Parameter(description = "New reaction data.") @RequestBody String reaction
    ) {
        if (!bingoService.getById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<String> result = bingoService.update(id, reaction);
        if (result.isPresent()) {
            return ResponseEntity
                    .ok()
                    .body(result.get());
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /reaction/:id/empty -> checks if reaction is empty.
     *
     * @param reaction Reaction to check
     * @return True if reaction is empty
     */
    @Operation(summary = "Checks if reaction is empty.")
    @RequestMapping(value = "/reaction/empty", method = RequestMethod.POST)
    public ResponseEntity<Object> isEmptyReaction(
            @Parameter(description = "Reaction to check.") @RequestBody String reaction) {
        Optional<Boolean> result = calculationService.isEmptyReaction(reaction);

        if (result.isPresent()) {
            return ResponseEntity
                    .ok().body(Collections.singletonMap("empty", result.get()));
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /reaction/:id -> delete reaction.
     *
     * @param id Id of the reaction to delete
     */
    @Operation(summary = "Deletes the reaction.")
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteReaction(
            @Parameter(description = "Id of the reaction to delete.") @PathVariable String id
    ) {
        bingoService.delete(id);
        return ResponseEntity
                .ok()
                .build();
    }

    /**
     * POST /molecule/search/exact?options=:options -> exact search molecule.
     *
     * @param structure Molecule structure to search for
     * @param options   Search options
     * @return Returns molecule
     */
    @Operation(summary = "Searches for exact molecule.")
    @RequestMapping(value = "/molecule/search/exact", method = RequestMethod.POST)
    public ResponseEntity<List<String>> searchMoleculeExact(
            @Parameter(description = "Molecule structure to search for.") @RequestBody String structure,
            @Parameter(description = "Search options.") @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchMoleculeExact(structure, options));
    }


    /**
     * POST /molecule/search/substructure?options=:options -> substructure search molecule.
     *
     * @param structure Molecule substructure to search for
     * @param options   Search options
     * @return Returns molecule
     */
    @Operation(summary = "Searches for molecule by substructure.")
    @RequestMapping(value = "/molecule/search/substructure", method = RequestMethod.POST)
    public ResponseEntity<List<String>> searchMoleculeSubstructure(
            @Parameter(description = "Molecule substructure to search for.") @RequestBody String structure,
            @Parameter(description = "Search options.") @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchMoleculeSub(structure, options));
    }

    /**
     * POST /molecule/search/similarity?min=:min&max=:max&options=:options -> similarity search molecule.
     *
     * @param structure Molecule substructure to search for
     * @param min       Min similarity
     * @param max       Max similarity
     * @param options   Search options
     * @return Returns molecule
     */
    @Operation(summary = "Searches for molecule by similarity.")
    @RequestMapping(value = "/molecule/search/similarity", method = RequestMethod.POST)
    public ResponseEntity<List<String>> searchMoleculeSimilarity(
            @Parameter(description = "Molecule substructure to search for.") @RequestBody String structure,
            @Parameter(description = "Min similarity.") @RequestParam Float min,
            @Parameter(description = "Max similarity.") @RequestParam Float max,
            @Parameter(description = "Search options.") @RequestParam(required = false) String options
    ) {
        return ResponseEntity.ok(bingoService.searchMoleculeSim(structure, min, max, options));
    }

    /**
     * POST /molecule/search/molformula?options=:options ->  search molecule by formula.
     *
     * @param formula Molecule formula to search for
     * @param options Search options
     * @return Returns molecule
     */
    @Operation(summary = "Searches for molecule by formula.")
    @RequestMapping(value = "/molecule/search/molformula", method = RequestMethod.POST)
    public ResponseEntity<List<String>> searchMoleculeByMolFormula(
            @Parameter(description = "Molecule formula to search for.") @RequestBody String formula,
            @Parameter(description = "Search options.") @RequestParam(required = false) String options
    ) {
        return ResponseEntity.ok(bingoService.searchMoleculeMolFormula(formula, options));
    }

    /**
     * POST /reaction/search/exact?options=:options -> exact search reaction.
     *
     * @param structure Reaction structure to search for
     * @param options   Search options
     * @return Returns reaction
     */
    @Operation(summary = "Searches for exact reaction.")
    @RequestMapping(value = "/reaction/search/exact", method = RequestMethod.POST)
    public ResponseEntity<List<String>> searchReactionExact(
            @Parameter(description = "Reaction structure to search for.") @RequestBody String structure,
            @Parameter(description = "Search options.") @RequestParam(required = false) String options
    ) {
        return ResponseEntity.ok(bingoService.searchReactionExact(structure, options));
    }

    /**
     * POST /reaction/search/substructure?options=:options -> substructure search reaction.
     *
     * @param structure Reaction substructure to search for
     * @param options   Search options
     * @return Returns reaction
     */
    @Operation(summary = "Searches for reaction by substructure.")
    @RequestMapping(value = "/reaction/search/substructure", method = RequestMethod.POST)
    public ResponseEntity<List<String>> searchReactionSubstructure(
            @Parameter(description = "Reaction substructure to search for.") @RequestBody String structure,
            @Parameter(description = "Search options.") @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchReactionSub(structure, options));
    }

    /**
     * POST /reaction/search/similarity?min=:min&max=:max&options=:options -> similarity search reaction.
     *
     * @param structure Reaction substructure to search for
     * @param min       Min similarity
     * @param max       Max similarity
     * @param options   Search options
     * @return Returns reaction
     */
    @Operation(summary = "Searches for reaction by similarity.")
    @RequestMapping(value = "/reaction/search/similarity", method = RequestMethod.POST)
    public ResponseEntity<List<String>> searchReactionSimilarity(
            @Parameter(description = "Reaction substructure to search for.") @RequestBody String structure,
            @Parameter(description = "Min similarity.") @RequestParam Float min,
            @Parameter(description = "Max similarity.") @RequestParam Float max,
            @Parameter(description = "Search options.") @RequestParam(required = false) String options
    ) {
        return ResponseEntity.ok(bingoService.searchReactionSim(structure, min, max, options));
    }

    /**
     * POST /reaction/search/molformula?options=:options ->  search reaction by formula.
     *
     * @param formula Reaction formula to search for
     * @param options Search options
     * @return Returns reaction
     */
    @Operation(summary = "Searches for reaction by formula.")
    @RequestMapping(value = "/reaction/search/molformula", method = RequestMethod.POST)
    public ResponseEntity<List<String>> searchReactionByMolFormula(
            @Parameter(description = "Reaction formula to search for.") @RequestBody String formula,
            @Parameter(description = "Search options.") @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchReactionMolFormula(formula, options));
    }
}
