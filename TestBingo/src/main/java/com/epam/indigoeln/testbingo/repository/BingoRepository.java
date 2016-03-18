package com.epam.indigoeln.testbingo.repository;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BingoRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(BingoRepository.class);

    @Value("${bingo.max-inserts}")
    private Integer maxInserts;

    @Autowired
    private String molFile;

    @Autowired
    private String rxnFile;

    @Autowired
    private Indigo indigo;

    @Autowired
    private Bingo moleculeBingo;

    @Autowired
    private Bingo reactionBingo;

    public void testMoleculeRepository() {
        LOGGER.warn("Inserting " + maxInserts + " molecules...");

        for (int i = 0; i < maxInserts; ++i) {
            moleculeBingo.insert(indigo.loadMolecule(molFile));
        }

        LOGGER.warn(maxInserts + " molecules inserted.");
        LOGGER.warn("Searching molecule...");

        BingoObject searchResult = moleculeBingo.searchSub(indigo.loadQueryMolecule(molFile));

        List<Integer> result = new ArrayList<>();

        while (searchResult.next()) {
            result.add(searchResult.getCurrentId());
        }

        LOGGER.warn("Done searching. Found: " + result.size());
    }

    public void testReactionRepository() {
        LOGGER.warn("Inserting " + maxInserts + " reactions...");

        for (int i = 0; i < maxInserts; ++i) {
            reactionBingo.insert(indigo.loadReaction(rxnFile));
        }

        LOGGER.warn(maxInserts + " reactions inserted.");
        LOGGER.warn("Searching reaction...");

        BingoObject searchResult = reactionBingo.searchSub(indigo.loadQueryReaction(rxnFile));

        List<Integer> result = new ArrayList<>();

        while (searchResult.next()) {
            result.add(searchResult.getCurrentId());
        }

        LOGGER.warn("Done searching. Found: " + result.size());
    }
}
