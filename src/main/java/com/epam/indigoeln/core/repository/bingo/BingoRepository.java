package com.epam.indigoeln.core.repository.bingo;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.IndigoObject;

import java.util.Optional;

public abstract class BingoRepository {

    private final Object lock = new Object();

    protected Bingo database;

    public Optional<IndigoObject> getById(Integer id) {
        synchronized (lock) {
            return Optional.ofNullable(database.getRecordById(id));
        }
    }

    public Optional<Integer> insert(IndigoObject record) {
        synchronized (lock) {
            return Optional.ofNullable(database.insert(record));
        }
    }

    public void update(IndigoObject record, Integer id) {
        synchronized (lock) {
            database.delete(id);
            database.insert(record, id);
        }
    }

    public void delete(Integer id) {
        synchronized (lock) {
            database.delete(id);
        }
    }

    public BingoObject searchExact(IndigoObject query) {
        return database.searchExact(query);
    }

    public BingoObject searchExact(IndigoObject query, String options) {
        return database.searchExact(query, options);
    }

    public BingoObject searchSub(IndigoObject query) {
        return database.searchSub(query);
    }

    public BingoObject searchSub(IndigoObject query, String options) {
        return database.searchSub(query, options);
    }

    public BingoObject searchSim(IndigoObject query, Float min, Float max) {
        return database.searchSim(query, min, max);
    }

    public BingoObject searchSim(IndigoObject query, Float min, Float max, String metric) {
        return database.searchSim(query, min, max, metric);
    }

    public BingoObject searchMolFormula(String query) {
        return database.searchMolFormula(query);
    }

    public BingoObject searchMolFormula(String query, String options) {
        return database.searchMolFormula(query, options);
    }
}
