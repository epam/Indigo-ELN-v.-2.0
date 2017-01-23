package com.epam.indigoeln.core.repository.bingo;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BingoRepository {

    private static final Logger log = LoggerFactory.getLogger(BingoRepository.class);

    public static final String MOLECULE = "molecule";
    public static final String REACTION = "reaction";

    private final Object lock = new Object();

    private String type;
    private Bingo bingo;

    private Indigo indigo;

    public BingoRepository(String type, Bingo bingo, Indigo indigo) {
        this.indigo = indigo;
        this.type = type;
        this.bingo = bingo;
    }

    public String getById(Integer id) {
        synchronized (lock) {
            try {
                return file(bingo.getRecordById(id));
            } catch (Exception e) {
                log.warn("Cannot find " + type + " with id=" + id + ": " + e.getMessage());
            }
            return null;
        }
    }

    public Integer insert(String structure) {
        synchronized (lock) {
            try {
                return bingo.insert(load(structure));
            } catch (Exception e) {
                log.warn("Cannot insert " + type + ": " + e.getMessage());
            }
            return null;
        }
    }

    public void update(String structure, Integer id) {
        synchronized (lock) {
            try {
                try {
                    bingo.delete(id);
                } catch (Exception ignored) {
                }
                bingo.insert(load(structure), id);
            } catch (Exception e) {
                log.warn("Cannot update " + type + " with id=" + id + ": " + e.getMessage());
            }
        }
    }

    public void delete(Integer id) {
        synchronized (lock) {
            try {
                bingo.delete(id);
            } catch (Exception e) {
                log.warn("Cannot delete " + type + " with id=" + id + ": " + e.getMessage());
            }
        }
    }

    public List<Integer> searchExact(String query, String options) {
        synchronized (lock) {
            try {
                return result(bingo.searchExact(load(query), options));
            } catch (Exception e) {
                log.warn("Cannot search exact: " + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    public List<Integer> searchSub(String query, String options) {
        synchronized (lock) {
            try {
                return result(bingo.searchSub(query(query), options));
            } catch (Exception e) {
                log.warn("Cannot search sub: " + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    public List<Integer> searchSim(String query, Float min, Float max, String metric) {
        synchronized (lock) {
            try {
                return result(bingo.searchSim(load(query), min, max, metric));
            } catch (Exception e) {
                log.warn("Cannot search sim: " + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    public List<Integer> searchMolFormula(String query, String options) {
        synchronized (lock) {
            try {
                return result(bingo.searchMolFormula(query, options));
            } catch (Exception e) {
                log.warn("Cannot search molformula: " + e.getMessage());
            }
            return Collections.emptyList();
        }
    }

    private IndigoObject load(String structure) throws Exception {
        switch (type) {
            case MOLECULE:
                return indigo.loadMolecule(structure);
            case REACTION:
                return indigo.loadReaction(structure);
        }
        throw new Exception("Unknown type");
    }

    private IndigoObject query(String structure) throws Exception {
        switch (type) {
            case MOLECULE:
                return indigo.loadQueryMolecule(structure);
            case REACTION:
                return indigo.loadQueryReaction(structure);
        }
        throw new Exception("Unknown type");
    }

    private String file(IndigoObject object) throws Exception {
        switch (type) {
            case MOLECULE:
                return object.molfile();
            case REACTION:
                return object.rxnfile();
        }
        throw new Exception("Unknown type");
    }

    private List<Integer> result(BingoObject object) {
        List<Integer> result = new ArrayList<>();

        while (object.next()) {
            result.add(object.getCurrentId());
        }

        return result;
    }
}
