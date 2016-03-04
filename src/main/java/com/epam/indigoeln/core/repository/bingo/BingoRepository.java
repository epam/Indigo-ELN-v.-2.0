package com.epam.indigoeln.core.repository.bingo;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.IndigoObject;

public abstract class BingoRepository {

    private final Object lock = new Object();

    protected Bingo database;

    public IndigoObject getById(Integer id) {
        synchronized (lock) {
            return database.getRecordById(id);
        }
    }

    public Integer insert(IndigoObject record) {
        synchronized (lock) {
            return database.insert(record);
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
        synchronized (lock) {
            return database.searchExact(query);
        }
    }

    public BingoObject searchExact(IndigoObject query, String options) {
        synchronized (lock) {
            return database.searchExact(query, options);
        }
    }

    public BingoObject searchSub(IndigoObject query) {
        synchronized (lock) {
            return database.searchSub(query);
        }
    }

    public BingoObject searchSub(IndigoObject query, String options) {
        synchronized (lock) {
            return database.searchSub(query, options);
        }
    }

    public BingoObject searchSim(IndigoObject query, Float min, Float max) {
        synchronized (lock) {
            return database.searchSim(query, min, max);
        }
    }

    public BingoObject searchSim(IndigoObject query, Float min, Float max, String metric) {
        synchronized (lock) {
            return database.searchSim(query, min, max, metric);
        }
    }

    public BingoObject searchMolFormula(String query) {
        synchronized (lock) {
            return database.searchMolFormula(query);
        }
    }

    public BingoObject searchMolFormula(String query, String options) {
        synchronized (lock) {
            return database.searchMolFormula(query, options);
        }
    }
}
