package com.epam.indigo.bingodb.controller;

import com.epam.indigo.bingodb.common.BingoResult;
import com.epam.indigo.bingodb.common.ErrorHandler;
import com.epam.indigo.bingodb.service.BingoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/molecule")
public class MoleculeController {

    @Autowired
    private BingoService bingoService;

    @ControllerAdvice
    static class JsonpAdvice extends AbstractJsonpResponseBodyAdvice {

        public JsonpAdvice() {
            super("callback");
        }

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BingoResult getMolecule(@PathVariable Integer id, @RequestParam(value = "width", required=false) Integer width,
                                   @RequestParam(value = "height", required=false) Integer height) {
        try {
            return BingoResult.success().withId(id).withStructure(bingoService.getMolecule(id));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot get Molecule with id=%s: %s", id, e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public BingoResult insertMolecule(@RequestBody String molecule) {
        try {
            return BingoResult.success().withId(bingoService.insertMolecule(molecule));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot insert Molecule to Database: %s", e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public BingoResult updateMolecule(@PathVariable Integer id, @RequestBody String molecule) {
        try {
            bingoService.updateMolecule(id, molecule);
            return BingoResult.success();
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot update Molecule in Database with id=%s: %s", id, e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public BingoResult deleteMolecule(@PathVariable Integer id) {
        try {
            bingoService.deleteMolecule(id);
            return BingoResult.success();
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot delete Molecule with id=%s: %s", id, e.getMessage());
        }
    }

    @RequestMapping(value = "/search/exact", method = RequestMethod.POST)
    public BingoResult searchMoleculeExact(@RequestBody String molecule, @RequestParam(required = false) String options) {
        try {
            return BingoResult.success().withSearchResult(bingoService.searchMoleculeExact(molecule, options));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot execute Molecule Exact Search: %s", e.getMessage());
        }
    }

    @RequestMapping(value = "/search/substructure", method = RequestMethod.POST)
    public BingoResult searchMoleculeSub(@RequestBody String molecule, @RequestParam(required = false) String options) {
        try {
            return BingoResult.success().withSearchResult(bingoService.searchMoleculeSub(molecule, options));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot execute Molecule Substructure Search: %s", e.getMessage());
        }
    }

    @RequestMapping(value = "/search/similarity", method = RequestMethod.POST)
    public BingoResult searchMoleculeSim(@RequestBody String molecule, @RequestParam Float min, @RequestParam Float max, @RequestParam(required = false) String metric) {
        try {
            return BingoResult.success().withSearchResult(bingoService.searchMoleculeSim(molecule, min, max, metric));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot execute Molecule Substructure Search: %s", e.getMessage());
        }
    }

    @RequestMapping(value = "/search/molformula", method = RequestMethod.POST)
    public BingoResult searchMoleculeMolFormula(@RequestBody String molFormula, @RequestParam(required = false) String options) {
        try {
            return BingoResult.success().withSearchResult(bingoService.searchMoleculeMolFormula(molFormula, options));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot execute Molecule MolFormula Search: %s", e.getMessage());
        }
    }
}
