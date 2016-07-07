package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Api
@RestController
@RequestMapping("/api/bingodb")
public class BingoResource {

    private static final String BINGODB_MOLECULE = "BingoDB Molecule";
    private static final String BINGODB_REACTION = "BingoDB Reaction";

    private static final String BINGODB_MOLECULE_PATH = "/api/bingodb/molecule/";
    private static final String BINGODB_REACTION_PATH = "/api/bingodb/reaction/";

    @Autowired
    private BingoService bingoService;

    /**
     * GET /molecule/:id -> get structure of molecule
     */
    @ApiOperation(value = "Returns the structure of molecule.", produces = "application/json")
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMolecule(
            @ApiParam("Identifier of the molecule.") @PathVariable Integer id
        ) {
        return bingoService
                .getMolecule(id)
                .map(molecule -> new ResponseEntity<>(molecule, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /molecule/ -> create new molecule
     */
    @ApiOperation(value = "Creates a new molecule.", produces = "application/json")
    @RequestMapping(value = "/molecule/", method = RequestMethod.POST)
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> addMolecule(
            @ApiParam("The molecule to create.") @RequestBody String molecule
        ) throws URISyntaxException {
        Optional<Integer> result = bingoService.insertMolecule(molecule);

        if (result.isPresent()) {
            return ResponseEntity
                    .created(new URI(BINGODB_MOLECULE_PATH + result.get()))
                    .headers(HeaderUtil.createEntityCreateAlert(BINGODB_MOLECULE, result.get().toString()))
                    .body(result.get().toString());
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /molecule/:id -> update molecule
     */
    @ApiOperation(value = "Updates the molecule.", produces = "application/json")
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.PUT)
    public ResponseEntity<String> updateMolecule(
            @ApiParam("Id of the molecule to update.") @PathVariable Integer id,
            @ApiParam("New molecule data.") @RequestBody String molecule
        ) {
        if (!bingoService.getMolecule(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        bingoService.updateMolecule(id, molecule);

        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityUpdateAlert(BINGODB_MOLECULE, id.toString()))
                .body(id.toString());
    }

    /**
     * DELETE /molecule/:id -> delete molecule
     */
    @ApiOperation(value = "Deletes the molecule.", produces = "application/json")
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteTemplate(
            @ApiParam("Id of the molecule to delete.") @PathVariable Integer id
        ) {
        bingoService.deleteMolecule(id);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityDeleteAlert(BINGODB_MOLECULE, id.toString()))
                .build();
    }

    /**
     * GET /reaction/:id -> get reaction structure
     */
    @ApiOperation(value = "Returns structure of the reaction.", produces = "application/json")
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getReaction(
            @ApiParam("Id of the reaction.") @PathVariable Integer id
        ) {
        return bingoService
                .getReaction(id)
                .map(molecule -> new ResponseEntity<>(molecule, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /reaction/ -> create new reaction
     */
    @ApiOperation(value = "Creates new reaction.", produces = "application/json")
    @RequestMapping(value = "/reaction/", method = RequestMethod.POST)
    public ResponseEntity<String> addReaction(
            @ApiParam("Reaction to create.") @RequestBody String reaction
        ) throws URISyntaxException {
        Optional<Integer> result = bingoService.insertReaction(reaction);

        if (result.isPresent()) {
            return ResponseEntity
                    .created(new URI(BINGODB_REACTION_PATH + result.get()))
                    .headers(HeaderUtil.createEntityCreateAlert(BINGODB_REACTION, result.get().toString()))
                    .body(result.get().toString());
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /reaction/:id -> update existing reaction
     */
    @ApiOperation(value = "Updates the reaction.", produces = "application/json")
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.PUT)
    public ResponseEntity<String> updateReaction(
            @ApiParam("Id of the reaction to update.") @PathVariable Integer id,
            @ApiParam("New reaction data.") @RequestBody String reaction
        ) {
        if (!bingoService.getReaction(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        bingoService.updateReaction(id, reaction);

        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityUpdateAlert(BINGODB_REACTION, id.toString()))
                .body(id.toString());
    }

    /**
     * DELETE /reaction/:id -> delete reaction
     */
    @ApiOperation(value = "Deletes the reaction.", produces = "application/json")
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteReaction(
            @ApiParam("Id of the reaction to delete.") @PathVariable Integer id
        ) {
        bingoService.deleteReaction(id);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityDeleteAlert(BINGODB_REACTION, id.toString()))
                .build();
    }

    /**
     * POST /molecule/search/exact?options=:options -> exact search molecule
     */
    @ApiOperation(value = "Searches for exact molecule.", produces = "application/json")
    @RequestMapping(value = "/molecule/search/exact", method = RequestMethod.POST)
    public ResponseEntity<List<Integer>> searchMoleculeExact(
            @ApiParam("Molecule structure to search for.") @RequestBody String structure,
            @ApiParam("Search options.") @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchMoleculeExact(structure, options));
    }


    /**
     * POST /molecule/search/substructure?options=:options -> substructure search molecule
     */
    @ApiOperation(value = "Searches for molecule by substructure.", produces = "application/json")
    @RequestMapping(value = "/molecule/search/substructure", method = RequestMethod.POST)
    public ResponseEntity<List<Integer>> searchMoleculeSubstructure(
            @ApiParam("Molecule substructure to search for.") @RequestBody String structure,
            @ApiParam("Search options.") @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchMoleculeSub(structure, options));
    }

    /**
     * POST /molecule/search/similarity?min=:min&max=:max&options=:options -> similarity search molecule
     */
    @ApiOperation(value = "Searches for molecule by similarity.", produces = "application/json")
    @RequestMapping(value = "/molecule/search/similarity", method = RequestMethod.POST)
    public ResponseEntity<List<Integer>> searchMoleculeSimilarity(
            @ApiParam("Molecule substructure to search for.") @RequestBody String structure,
            @ApiParam("Min similarity.") @RequestParam Float min,
            @ApiParam("Max similarity.") @RequestParam Float max,
            @ApiParam("Search options.") @RequestParam(required = false) String options
        ) {
        return ResponseEntity.ok(bingoService.searchMoleculeSim(structure, min, max, options));
    }

    /**
     * POST /molecule/search/molformula?options=:options ->  search molecule by formula
     */
    @ApiOperation(value = "Searches for molecule by formula.", produces = "application/json")
    @RequestMapping(value = "/molecule/search/molformula", method = RequestMethod.POST)
    public ResponseEntity<List<Integer>> searchMoleculeByMolFormula(
            @ApiParam("Molecule formula to search for.") @RequestBody String formula,
            @ApiParam("Search options.") @RequestParam(required = false) String options
        ) {
        return ResponseEntity.ok(bingoService.searchMoleculeMolFormula(formula, options));
    }

    /**
     * POST /reaction/search/exact?options=:options -> exact search reaction
     */
    @ApiOperation(value = "Searches for exact reaction.", produces = "application/json")
    @RequestMapping(value = "/reaction/search/exact", method = RequestMethod.POST)
    public ResponseEntity<List<Integer>> searchReactionExact(
            @ApiParam("Reaction structure to search for.") @RequestBody String structure,
            @ApiParam("Search options.") @RequestParam(required = false) String options
        ) {
        return ResponseEntity.ok(bingoService.searchReactionExact(structure, options));
    }

    /**
     * POST /reaction/search/substructure?options=:options -> substructure search reaction
     */
    @ApiOperation(value = "Searches for reaction by substructure.", produces = "application/json")
    @RequestMapping(value = "/reaction/search/substructure", method = RequestMethod.POST)
    public ResponseEntity<List<Integer>> searchReactionSubstructure(
            @ApiParam("Reaction substructure to search for.") @RequestBody String structure,
            @ApiParam("Search options.") @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchReactionSub(structure, options));
    }

    /**
     * POST /reaction/search/similarity?min=:min&max=:max&options=:options -> similarity search reaction
     */
    @ApiOperation(value = "Searches for reaction by similarity.", produces = "application/json")
    @RequestMapping(value = "/reaction/search/similarity", method = RequestMethod.POST)
    public ResponseEntity<List<Integer>> searchReactionSimilarity(
            @ApiParam("Reaction substructure to search for.") @RequestBody String structure,
            @ApiParam("Min similarity.") @RequestParam Float min,
            @ApiParam("Max similarity.") @RequestParam Float max,
            @ApiParam("Search options.") @RequestParam(required = false) String options
        ) {
        return ResponseEntity.ok(bingoService.searchReactionSim(structure, min, max, options));
    }

    /**
     * POST /reaction/search/molformula?options=:options ->  search reaction by formula
     */
    @ApiOperation(value = "Searches for reaction by formula.", produces = "application/json")
    @RequestMapping(value = "/reaction/search/molformula", method = RequestMethod.POST)
    public ResponseEntity<List<Integer>> searchReactionByMolFormula(
            @ApiParam("Reaction formula to search for.") @RequestBody String formula,
            @ApiParam("Search options.") @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchReactionMolFormula(formula, options));
    }
}
