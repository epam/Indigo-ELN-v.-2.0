package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.integration.BingoResult;
import com.epam.indigoeln.core.service.bingodb.BingoDbIntegrationService;
import com.epam.indigoeln.web.rest.util.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.epam.indigoeln.core.service.search.SearchServiceConstants.*;

@RestController
@RequestMapping("/api/bingodb")
public class BingoDbIntegrationResource {

    private static final String BINGODB_MOLECULE = "BingoDB Molecule";
    private static final String BINGODB_REACTION = "BingoDB Reaction";

    private static final String BINGODB_MOLECULE_PATH = "/api/bingodb/molecule/";
    private static final String BINGODB_REACTION_PATH = "/api/bingodb/reaction/";

    private static final String PARAM_OPTIONS = "options";
    private static final String PARAM_MIN = "min";
    private static final String PARAM_MAX = "max";

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
                    .headers(HeaderUtil.createEntityCreateAlert(BINGODB_MOLECULE, result.getId().toString()))
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
                HeaderUtil.createEntityDeleteAlert(BINGODB_MOLECULE, id.toString())).build();
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
                .headers(HeaderUtil.createEntityCreateAlert(BINGODB_REACTION, result.getId().toString()))
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
                HeaderUtil.createEntityDeleteAlert(BINGODB_REACTION, id.toString())).build();
    }

    /**
     * POST /molecule/search/exact?options=:options -> exact search molecule
     */
    @RequestMapping(value = "/molecule/search/exact",
                    method = RequestMethod.POST,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchMoleculeExact(@RequestBody String structure,
                                                 @RequestParam(required = false) String options) {
        return search(structure, CHEMISTRY_SEARCH_EXACT,  getParamsWithOptions(options), false);
    }


    /**
     * POST /molecule/search/substructure?options=:options -> substructure search molecule
     */
    @RequestMapping(value = "/molecule/search/substructure",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchMoleculeSubstructure(@RequestBody String structure,
                                                        @RequestParam(required = false) String options) {
        return search(structure, CHEMISTRY_SEARCH_SUBSTRUCTURE, getParamsWithOptions(options), false);
    }

    /**
     * POST /molecule/search/similarity?min=:min&max=:max&options=:options -> substructure search molecule
     */
    @RequestMapping(value = "/molecule/search/similarity",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchMoleculeSimilarity(@RequestBody String structure,
                                                      @RequestParam Float min,
                                                      @RequestParam Float max,
                                                      @RequestParam(required = false) String options) {
        Map<String, String> params = getParamsWithOptions(options);
        params.put(PARAM_MIN, min.toString());
        params.put(PARAM_MAX, max.toString());
        return search(structure, CHEMISTRY_SEARCH_SIMILARITY, params, false);
    }

    /**
     * POST /molecule/search/molformula?options=:options ->  search molecule by formula
     */
    @RequestMapping(value = "/molecule/search/molformula",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchMoleculeByMolFormula(@RequestBody String molFormula,
                                                        @RequestParam(required = false) String options) {
        return search(molFormula, CHEMISTRY_SEARCH_MOLFORMULA, getParamsWithOptions(options), false);
    }

    /**
     * POST /reaction/search/exact?options=:options -> exact search reaction
     */
    @RequestMapping(value = "/reaction/search/exact",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchReactionExact(@RequestBody String structure,
                                                 @RequestParam(required = false) String options) {
        return search(structure, CHEMISTRY_SEARCH_EXACT,  getParamsWithOptions(options), true);
    }


    /**
     * POST /reaction/search/substructure?options=:options -> substructure search reaction
     */
    @RequestMapping(value = "/reaction/search/substructure",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchReactionSubstructure(@RequestBody String structure,
                                                        @RequestParam(required = false) String options) {
        return search(structure, CHEMISTRY_SEARCH_SUBSTRUCTURE, getParamsWithOptions(options), true);
    }

    /**
     * POST /reaction/search/similarity?min=:min&max=:max&options=:options -> substructure search reaction
     */
    @RequestMapping(value = "/reaction/search/similarity",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchReactionSimilarity(@RequestBody String structure,
                                                      @RequestParam Float min,
                                                      @RequestParam Float max,
                                                      @RequestParam(required = false) String options) {
        Map<String, String> params = getParamsWithOptions(options);
        params.put(PARAM_MIN, min.toString());
        params.put(PARAM_MAX, max.toString());
        return search(structure, CHEMISTRY_SEARCH_SIMILARITY, params, true);
    }

    /**
     * POST /reaction/search/molformula?options=:options ->  search reaction by formula
     */
    @RequestMapping(value = "/reaction/search/molformula",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchReactionByMolFormula(@RequestBody String molFormula,
                                                        @RequestParam(required = false) String options) {
        return search(molFormula, CHEMISTRY_SEARCH_MOLFORMULA, getParamsWithOptions(options), true);
    }


    @SuppressWarnings("unchecked")
    private ResponseEntity search(String body,
                                  String type,
                                  Map<String, String> params,
                                  boolean isReaction) {

        BingoResult result = isReaction ? bingoDbService.searchReaction(body, type, params) :
                bingoDbService.searchMolecule(body, type, params);
        return result.isSuccess() ? ResponseEntity.ok().body(result.getSearchResult()) :
                new ResponseEntity(result.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, String> getParamsWithOptions(String options) {
        Map<String, String> params = new HashMap<>();
        Optional.ofNullable(options).ifPresent(opt -> params.put(PARAM_OPTIONS, opt));
        return params;
    }
}
