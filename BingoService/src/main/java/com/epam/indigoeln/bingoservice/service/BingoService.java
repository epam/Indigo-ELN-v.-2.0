package com.epam.indigoeln.bingoservice.service;

import com.epam.indigo.Bingo;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(BingoService.class);

    private static final Object INDIGO_LOCK = new Object();
    private static final Object MOLECULE_LOCK = new Object();
    private static final Object REACTION_LOCK = new Object();

    @Value("${bingo.database.folder}/molecules")
    private String moleculeDatabaseFolder;

    @Value("${bingo.database.folder}/reactions")
    private String reactionDatabaseFolder;

    private Indigo indigo;
    private Bingo moleculeDatabase;
    private Bingo reactionDatabase;

    public String getMolecule(Integer id) {
        synchronized (MOLECULE_LOCK) {
            return moleculeDatabase.getRecordById(id).molfile();
        }
    }

    public Integer insertMolecule(String molecule) {
        IndigoObject object;

        synchronized (INDIGO_LOCK) {
            object = indigo.loadMolecule(molecule);
        }

        synchronized (MOLECULE_LOCK) {
            return moleculeDatabase.insert(object);
        }
    }

    public void updateMolecule(Integer id, String molecule) {
        IndigoObject object;

        synchronized (INDIGO_LOCK) {
            object = indigo.loadMolecule(molecule);
        }

        synchronized (MOLECULE_LOCK) {
            moleculeDatabase.delete(id);
            moleculeDatabase.insert(object, id);
        }
    }

    public void deleteMolecule(Integer id) {
        synchronized (MOLECULE_LOCK) {
            moleculeDatabase.delete(id);
        }
    }

    @PostConstruct
    public void initDatabase() throws IOException {
        synchronized (INDIGO_LOCK) {
            if (indigo == null) {
                indigo = initIndigo();
            }
        }
        synchronized (MOLECULE_LOCK) {
            if (moleculeDatabase == null) {
                moleculeDatabase = initDatabase(moleculeDatabaseFolder, "molecule", indigo);
            }
        }
        synchronized (REACTION_LOCK) {
            if (reactionDatabase == null) {
                reactionDatabase = initDatabase(reactionDatabaseFolder, "reaction", indigo);
            }
        }
    }

    @PreDestroy
    public void closeDatabase() {
        synchronized (MOLECULE_LOCK) {
            closeDatabase(moleculeDatabase);
        }
        synchronized (REACTION_LOCK) {
            closeDatabase(reactionDatabase);
        }
    }

    private static Indigo initIndigo() {
        return new Indigo();
    }

    private static Bingo initDatabase(String folder, String type, Indigo indigo) throws IOException {
        Bingo database;

        if (!Files.exists(Paths.get(folder))) {
            Files.createDirectories(Paths.get(folder));
            database = Bingo.createDatabaseFile(indigo, folder, type);
        } else {
            database = Bingo.loadDatabaseFile(indigo, folder);
        }

        database.optimize();

        return database;
    }

    private static void closeDatabase(Bingo database) {
        try {
            if (database != null) {
                database.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot close database", e);
        }
    }
}
