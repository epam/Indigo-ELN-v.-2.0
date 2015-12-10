package com.epam.indigo.bingodb.controller;

import com.epam.indigo.bingodb.common.BingoResult;
import com.epam.indigo.bingodb.common.ErrorHandler;
import com.epam.indigo.bingodb.service.BingoService;
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

    @RequestMapping(value = "/search/exact", method = RequestMethod.POST)
    public BingoResult searchReactionExact(@RequestBody String reaction, @RequestParam(required = false) String options) {
        try {
            return BingoResult.success().withSearchResult(bingoService.searchReactionExact(reaction, options));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot execute Reaction Exact Search: %s", e.getMessage());
        }
    }

    @RequestMapping(value = "/search/substructure", method = RequestMethod.POST)
    public BingoResult searchReactionSub(@RequestBody String reaction, @RequestParam(required = false) String options) {
        try {
            return BingoResult.success().withSearchResult(bingoService.searchReactionSub(reaction, options));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot execute Reaction Substructure Search: %s", e.getMessage());
        }
    }

    @RequestMapping(value = "/search/similarity", method = RequestMethod.POST)
    public BingoResult searchReactionSim(@RequestBody String reaction, @RequestParam Float min, @RequestParam Float max, @RequestParam(required = false) String metric) {
        try {
            return BingoResult.success().withSearchResult(bingoService.searchReactionSim(reaction, min, max, metric));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot execute Reaction Substructure Search: %s", e.getMessage());
        }
    }

    @RequestMapping(value = "/search/molformula", method = RequestMethod.POST)
    public BingoResult searchReactionMolFormula(@RequestBody String molFormula, @RequestParam(required = false) String options) {
        try {
            return BingoResult.success().withSearchResult(bingoService.searchReactionMolFormula(molFormula, options));
        } catch (Exception e) {
            return ErrorHandler.handleError(e, "Cannot execute Reaction MolFormula Search: %s", e.getMessage());
        }
    }
}
