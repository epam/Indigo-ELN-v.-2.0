package com.epam.indigoeln.bingoservice.service;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class BingoService {

    private static final Logger log = LoggerFactory.getLogger(BingoService.class);

    @Value("${bingo.database.folder}/molecules")
    private String moleculeDatabaseFolder;

    @Value("${bingo.database.folder}/reactions")
    private String reactionDatabaseFolder;

    private Indigo indigo;

    private Bingo moleculeDatabase;
    private Bingo reactionDatabase;

    @PostConstruct
    public void initBingo() throws IOException {
        indigo = new Indigo();

        if (!Files.exists(Paths.get(moleculeDatabaseFolder))) {
            Files.createDirectories(Paths.get(moleculeDatabaseFolder));
            moleculeDatabase = Bingo.createDatabaseFile(indigo, moleculeDatabaseFolder, "molecule");
        } else {
            moleculeDatabase = Bingo.loadDatabaseFile(indigo, moleculeDatabaseFolder);
        }

        if (!Files.exists(Paths.get(reactionDatabaseFolder))) {
            Files.createDirectories(Paths.get(reactionDatabaseFolder));
            reactionDatabase = Bingo.createDatabaseFile(indigo, reactionDatabaseFolder, "reaction");
        } else {
            reactionDatabase = Bingo.loadDatabaseFile(indigo, reactionDatabaseFolder);
        }
    }

    @PreDestroy
    public void closeBingo() {
        try {
            if (moleculeDatabase != null) {
                moleculeDatabase.close();
            }
        } catch (Exception e) {
            log.warn("Cannot clone Molecule Database", e);
        }
        try {
            if (reactionDatabase != null) {
                reactionDatabase.close();
            }
        } catch (Exception e) {
            log.warn("Cannot clone Reaction Database", e);
        }
    }
}
