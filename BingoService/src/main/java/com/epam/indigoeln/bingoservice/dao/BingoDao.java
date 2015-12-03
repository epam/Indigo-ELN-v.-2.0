package com.epam.indigoeln.bingoservice.dao;

import com.epam.indigo.Bingo;
import com.epam.indigo.IndigoObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BingoDao {

    @Autowired
    private Bingo moleculeDatabase;

    @Autowired
    private Bingo reactionDatabase;

    public IndigoObject getMoleculeById(Integer id) {
        return moleculeDatabase.getRecordById(id);
    }

    public Integer insertMolecule(IndigoObject molecule) {
        return moleculeDatabase.insert(molecule);
    }

    public Integer insertMolecule(IndigoObject molecule, Integer id) {
        return moleculeDatabase.insert(molecule, id);
    }

    public void deleteMolecule(Integer id) {
        moleculeDatabase.delete(id);
    }

    public IndigoObject getReactionById(Integer id) {
        return reactionDatabase.getRecordById(id);
    }

    public Integer insertReaction(IndigoObject reaction) {
        return reactionDatabase.insert(reaction);
    }

    public Integer insertReaction(IndigoObject reaction, Integer id) {
        return reactionDatabase.insert(reaction, id);
    }

    public void deleteReaction(Integer id) {
        reactionDatabase.delete(id);
    }
}
