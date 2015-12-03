package com.epam.indigoeln.bingoservice.controller;

import com.epam.indigoeln.bingoservice.common.BingoResult;
import com.epam.indigoeln.bingoservice.common.ErrorHandler;
import com.epam.indigoeln.bingoservice.service.BingoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reaction")
public class ReactionController {

    @Autowired
    private BingoService bingoService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BingoResult getReaction(@PathVariable Integer id) {
        try {
            return BingoResult.success().withId(id).withStructure(bingoService.getReaction(id));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot get Reaction with id=%s: %s", id, e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public BingoResult insertReaction(@RequestBody String reaction) {
        try {
            return BingoResult.success().withId(bingoService.insertReaction(reaction));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot insert Reaction to Database: %s", e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public BingoResult updateReaction(@PathVariable Integer id, @RequestBody String reaction) {
        try {
            bingoService.updateReaction(id, reaction);
            return BingoResult.success();
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot update Reaction in Database with id=%s: %s", id, e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public BingoResult deleteReaction(@PathVariable Integer id) {
        try {
            bingoService.deleteReaction(id);
            return BingoResult.success();
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot delete Reaction with id=%s: %s", id, e.getMessage());
        }
    }
}
