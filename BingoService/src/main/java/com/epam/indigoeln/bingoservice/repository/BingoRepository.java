package com.epam.indigoeln.bingoservice.repository;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.IndigoObject;

public abstract class BingoRepository {

    protected Bingo database;

    public IndigoObject getById(Integer id) {
        return database.getRecordById(id);
    }

    public Integer insert(IndigoObject record) {
        return database.insert(record);
    }

    public Integer insert(IndigoObject record, Integer id) {
        return database.insert(record, id);
    }

    public void delete(Integer id) {
        database.delete(id);
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
