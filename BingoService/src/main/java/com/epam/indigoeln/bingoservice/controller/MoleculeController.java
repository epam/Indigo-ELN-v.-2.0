package com.epam.indigoeln.bingoservice.controller;

import com.epam.indigoeln.bingoservice.common.BingoResult;
import com.epam.indigoeln.bingoservice.service.BingoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/molecule")
public class MoleculeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoleculeController.class);

    @Autowired
    private BingoService bingoService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BingoResult getMolecule(@PathVariable Integer id) {
        try {
            return BingoResult.success().withId(id).withStructure(bingoService.getMolecule(id));
        } catch (Exception e) {
            String errorMessage = "Cannot get Molecule with id=" + id + ": " + e.getMessage();
            LOGGER.error(errorMessage, e);
            return BingoResult.failure().withErrorMessage(errorMessage);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public BingoResult insertMolecule(@RequestBody String molecule) {
        try {
            return BingoResult.success().withId(bingoService.insertMolecule(molecule));
        } catch (Exception e) {
            String errorMessage = "Cannot insert Molecule to Database: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            return BingoResult.failure().withErrorMessage(errorMessage);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public BingoResult updateMolecule(@PathVariable Integer id, @RequestBody String molecule) {
        try {
            bingoService.updateMolecule(id, molecule);
            return BingoResult.success();
        } catch (Exception e) {
            String errorMessage = "Cannot update Molecule in Database with id=" + id + ": " + e.getMessage();
            LOGGER.error(errorMessage, e);
            return BingoResult.failure().withErrorMessage(errorMessage);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public BingoResult deleteMolecule(@PathVariable Integer id) {
        try {
            bingoService.deleteMolecule(id);
            return BingoResult.success();
        } catch (Exception e) {
            String errorMessage = "Cannot delete Molecule with id=" + id + ": " + e.getMessage();
            LOGGER.error(errorMessage, e);
            return BingoResult.failure().withErrorMessage(errorMessage);
        }
    }
}
