package com.epam.indigoeln.bingoservice.controller;

import com.epam.indigoeln.bingoservice.common.BingoResult;
import com.epam.indigoeln.bingoservice.service.BingoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reaction")
public class ReactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactionController.class);

    @Autowired
    private BingoService bingoService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BingoResult getReaction(@PathVariable Integer id) {
        try {
            return BingoResult.success().withId(id).withStructure(bingoService.getReaction(id));
        } catch (Exception e) {
            String errorMessage = "Cannot get Reaction with id=" + id + ": " + e.getMessage();
            LOGGER.error(errorMessage, e);
            return BingoResult.failure().withErrorMessage(errorMessage);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public BingoResult insertReaction(@RequestBody String reaction) {
        try {
            return BingoResult.success().withId(bingoService.insertReaction(reaction));
        } catch (Exception e) {
            String errorMessage = "Cannot insert Reaction to Database: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            return BingoResult.failure().withErrorMessage(errorMessage);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public BingoResult updateReaction(@PathVariable Integer id, @RequestBody String reaction) {
        try {
            bingoService.updateReaction(id, reaction);
            return BingoResult.success();
        } catch (Exception e) {
            String errorMessage = "Cannot update Reaction in Database with id=" + id + ": " + e.getMessage();
            LOGGER.error(errorMessage, e);
            return BingoResult.failure().withErrorMessage(errorMessage);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public BingoResult deleteReaction(@PathVariable Integer id) {
        try {
            bingoService.deleteReaction(id);
            return BingoResult.success();
        } catch (Exception e) {
            String errorMessage = "Cannot delete Reaction with id=" + id + ": " + e.getMessage();
            LOGGER.error(errorMessage, e);
            return BingoResult.failure().withErrorMessage(errorMessage);
        }
    }
}
