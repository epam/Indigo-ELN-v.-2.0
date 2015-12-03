package com.epam.indigo.bingoservice.controller;

import com.epam.indigo.bingoservice.common.BingoResult;
import com.epam.indigo.bingoservice.common.ErrorHandler;
import com.epam.indigo.bingoservice.service.BingoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/molecule")
public class MoleculeController {

    @Autowired
    private BingoService bingoService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BingoResult getMolecule(@PathVariable Integer id) {
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
}
