package com.epam.indigoeln.bingodb.web;

import com.epam.indigoeln.bingodb.service.BingoService;
import com.epam.indigoeln.bingodb.web.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchResource {

    private static final Logger log = LoggerFactory.getLogger(SearchResource.class);

    @Autowired
    private BingoService bingoService;

    @RequestMapping(method = RequestMethod.POST, path = "/molecule/exact")
    public ResponseEntity<ResponseDTO> searchMoleculeExact(@RequestBody String s,
                                                           @RequestParam(required = false) String options) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeExact(s, options)));
        } catch (Exception e) {
            log.warn("Cannot search molecule exact: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot search molecule exact with given structure"));
    }

    @RequestMapping(method = RequestMethod.POST, path = "/molecule/substructure")
    public ResponseEntity<ResponseDTO> searchMoleculeSub(@RequestBody String s,
                                                         @RequestParam(required = false) String options) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeSub(s, options)));
        } catch (Exception e) {
            log.warn("Cannot search molecule substructure: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot search molecule substructure with given structure"));
    }

    @RequestMapping(method = RequestMethod.POST, path = "/molecule/similarity")
    public ResponseEntity<ResponseDTO> searchMoleculeSimilarity(@RequestBody String s,
                                                                @RequestParam Float min,
                                                                @RequestParam Float max,
                                                                @RequestParam(required = false) String metric) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeSim(s, min, max, metric)));
        } catch (Exception e) {
            log.warn("Cannot search molecule similarity: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot search molecule similarity with given structure"));
    }

    @RequestMapping(method = RequestMethod.POST, path = "/molecule/molformula")
    public ResponseEntity<ResponseDTO> searchMoleculeMolFormula(@RequestBody String molFormula,
                                                                @RequestParam(required = false) String options) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchMoleculeMolFormula(molFormula, options)));
        } catch (Exception e) {
            log.warn("Cannot search molecule molformula: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot search molecule molformula with given mol formula"));
    }

    @RequestMapping(method = RequestMethod.POST, path = "/reaction/exact")
    public ResponseEntity<ResponseDTO> searchReactionExact(@RequestBody String s,
                                                           @RequestParam(required = false) String options) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionExact(s, options)));
        } catch (Exception e) {
            log.warn("Cannot search reaction exact: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot search reaction exact with given structure"));
    }

    @RequestMapping(method = RequestMethod.POST, path = "/reaction/substructure")
    public ResponseEntity<ResponseDTO> searchReactionSub(@RequestBody String s,
                                                         @RequestParam(required = false) String options) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionSub(s, options)));
        } catch (Exception e) {
            log.warn("Cannot search reaction substructure: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot search reaction substructure with given structure"));
    }

    @RequestMapping(method = RequestMethod.POST, path = "/reaction/similarity")
    public ResponseEntity<ResponseDTO> searchReactionSimilarity(@RequestBody String s,
                                                                @RequestParam Float min,
                                                                @RequestParam Float max,
                                                                @RequestParam(required = false) String metric) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionSim(s, min, max, metric)));
        } catch (Exception e) {
            log.warn("Cannot search reaction similarity: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot search reaction similarity with given structure"));
    }

    @RequestMapping(method = RequestMethod.POST, path = "/reaction/molformula")
    public ResponseEntity<ResponseDTO> searchReactionMolFormula(@RequestBody String molFormula,
                                                                @RequestParam(required = false) String options) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO(bingoService.searchReactionMolFormula(molFormula, options)));
        } catch (Exception e) {
            log.warn("Cannot search reaction molformula: " + e.getMessage());
        }
        return ResponseEntity.badRequest().body(new ResponseDTO("Cannot search reaction molformula with given mol formula"));
    }
}
