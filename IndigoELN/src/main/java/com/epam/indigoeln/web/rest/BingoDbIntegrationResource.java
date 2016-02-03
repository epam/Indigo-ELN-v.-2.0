package com.epam.indigoeln.web.rest;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.epam.indigoeln.web.rest.util.HeaderUtil;
import com.epam.indigoeln.core.service.bingodb.BingoDbIntegrationService;
import com.epam.indigoeln.core.integration.BingoResult;

@RestController
@RequestMapping("/api/bingodb")
public class BingoDbIntegrationResource {

    private static final String BINGODB_MOLECULE = "BingoDB Molecule";
    private static final String BINGODB_REACTION = "BingoDB Reaction";

    private static final String BINGODB_MOLECULE_PATH = "/api/bingodb/molecule/";
    private static final String BINGODB_REACTION_PATH = "/api/bingodb/reaction/";


    @Autowired
    private BingoDbIntegrationService bingoDbService;

    /**
     * GET /molecule/:id -> get structure of molecule
     */
    @RequestMapping(value = "/molecule/{id}",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMolecule(@PathVariable Integer id) {
        return bingoDbService.getMolecule(id)
                .map(molecule -> new ResponseEntity<>(molecule, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /molecule/ -> create new molecule
     */
    @RequestMapping(value = "/molecule/",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> addMolecule(@RequestBody String molecule) throws URISyntaxException {
        BingoResult result = bingoDbService.addMolecule(molecule);
        if(result.isSuccess()) {
            return ResponseEntity.created(new URI(BINGODB_MOLECULE_PATH + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(BINGODB_MOLECULE, result.getId().toString()))
                    .body(result.getId().toString());
        } else {
            return new ResponseEntity(result.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /molecule/:id -> update molecule
     */
    @RequestMapping(value = "/molecule/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> updateMolecule(@PathVariable Integer id,
                                                 @RequestBody  String molecule) {
        if(!bingoDbService.getMolecule(id).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        BingoResult result = bingoDbService.updateMolecule(id, molecule);
        if(result.isSuccess()) {
            return ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(BINGODB_MOLECULE, id.toString()))
                    .body(id.toString());
        } else {
            return new ResponseEntity(result.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /molecule/:id -> delete molecule
     */
    @RequestMapping(value = "/molecule/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteTemplate(@PathVariable Integer id) {
        bingoDbService.deleteMolecule(id);
        return ResponseEntity.ok().headers(
                HeaderUtil.createEntityDeletionAlert(BINGODB_MOLECULE, id.toString())).build();
    }

    /**
     * GET /reaction/:id -> get reaction structure
     */
    @RequestMapping(value = "/reaction/{id}",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getReaction(@PathVariable Integer id) {
        return bingoDbService.getReaction(id)
                .map(molecule -> new ResponseEntity<>(molecule, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /reaction/ -> create new reaction
     */
    @RequestMapping(value = "/reaction/",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> addReaction(@RequestBody String reaction) throws URISyntaxException {
        BingoResult result = bingoDbService.addReaction(reaction);
        if(result.isSuccess()) {
            return ResponseEntity.created(new URI(BINGODB_REACTION_PATH + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(BINGODB_REACTION, result.getId().toString()))
                .body(result.getId().toString());
        } else {
            return new ResponseEntity(result.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * PUT /reaction/:id -> update existing reaction
     */
    @RequestMapping(value = "/reaction/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> updateReaction(@PathVariable Integer id,
                                                 @RequestBody  String reaction) {
        if(!bingoDbService.getReaction(id).isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        BingoResult result = bingoDbService.updateReaction(id, reaction);
        if(result.isSuccess()) {
            return ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(BINGODB_REACTION, id.toString()))
                    .body(id.toString());
        } else {
            return new ResponseEntity(result.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /reaction/:id -> delete reaction
     */
    @RequestMapping(value = "/reaction/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteReaction(@PathVariable Integer id) {
        bingoDbService.deleteReaction(id);
        return ResponseEntity.ok().headers(
                HeaderUtil.createEntityDeletionAlert(BINGODB_REACTION, id.toString())).build();
    }
}
