package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.service.bingo.BingoService;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

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
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMolecule(@PathVariable Integer id) {
        return bingoService
                .getMolecule(id)
                .map(molecule -> new ResponseEntity<>(molecule, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /molecule/ -> create new molecule
     */
    @RequestMapping(value = "/molecule/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> addMolecule(@RequestBody String molecule) throws URISyntaxException {
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
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateMolecule(@PathVariable Integer id, @RequestBody String molecule) {
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
    @RequestMapping(value = "/molecule/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteTemplate(@PathVariable Integer id) {
        bingoService.deleteMolecule(id);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityDeleteAlert(BINGODB_MOLECULE, id.toString()))
                .build();
    }

    /**
     * GET /reaction/:id -> get reaction structure
     */
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getReaction(@PathVariable Integer id) {
        return bingoService
                .getReaction(id)
                .map(molecule -> new ResponseEntity<>(molecule, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /reaction/ -> create new reaction
     */
    @RequestMapping(value = "/reaction/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addReaction(@RequestBody String reaction) throws URISyntaxException {
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
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateReaction(@PathVariable Integer id, @RequestBody String reaction) {
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
    @RequestMapping(value = "/reaction/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteReaction(@PathVariable Integer id) {
        bingoService.deleteReaction(id);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityDeleteAlert(BINGODB_REACTION, id.toString()))
                .build();
    }

    /**
     * POST /molecule/search/exact?options=:options -> exact search molecule
     */
    @RequestMapping(value = "/molecule/search/exact", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchMoleculeExact(@RequestBody String structure, @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchMoleculeExact(structure, options));
    }


    /**
     * POST /molecule/search/substructure?options=:options -> substructure search molecule
     */
    @RequestMapping(value = "/molecule/search/substructure", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchMoleculeSubstructure(@RequestBody String structure, @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchMoleculeSub(structure, options));
    }

    /**
     * POST /molecule/search/similarity?min=:min&max=:max&options=:options -> similarity search molecule
     */
    @RequestMapping(value = "/molecule/search/similarity", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchMoleculeSimilarity(@RequestBody String structure,
                                                                  @RequestParam Float min,
                                                                  @RequestParam Float max,
                                                                  @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchMoleculeSim(structure, min, max, options));
    }

    /**
     * POST /molecule/search/molformula?options=:options ->  search molecule by formula
     */
    @RequestMapping(value = "/molecule/search/molformula", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchMoleculeByMolFormula(@RequestBody String molFormula, @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchMoleculeMolFormula(molFormula, options));
    }

    /**
     * POST /reaction/search/exact?options=:options -> exact search reaction
     */
    @RequestMapping(value = "/reaction/search/exact", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchReactionExact(@RequestBody String structure, @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchReactionExact(structure, options));
    }


    /**
     * POST /reaction/search/substructure?options=:options -> substructure search reaction
     */
    @RequestMapping(value = "/reaction/search/substructure", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchReactionSubstructure(@RequestBody String structure, @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchReactionSub(structure, options));
    }

    /**
     * POST /reaction/search/similarity?min=:min&max=:max&options=:options -> similarity search reaction
     */
    @RequestMapping(value = "/reaction/search/similarity", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchReactionSimilarity(@RequestBody String structure,
                                                                  @RequestParam Float min,
                                                                  @RequestParam Float max,
                                                                  @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchReactionSim(structure, min, max, options));
    }

    /**
     * POST /reaction/search/molformula?options=:options ->  search reaction by formula
     */
    @RequestMapping(value = "/reaction/search/molformula", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> searchReactionByMolFormula(@RequestBody String molFormula, @RequestParam(required = false) String options) {
        return ResponseEntity.ok(bingoService.searchReactionMolFormula(molFormula, options));
    }
}
