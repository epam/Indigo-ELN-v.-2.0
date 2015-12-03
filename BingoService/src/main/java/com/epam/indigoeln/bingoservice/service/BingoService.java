package com.epam.indigoeln.bingoservice.service;

import com.epam.indigo.Indigo;
import com.epam.indigoeln.bingoservice.dao.BingoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BingoService {

    @Autowired
    private Indigo indigo;

    @Autowired
    private BingoDao bingoDao;

    public String getMolecule(Integer id) {
        return bingoDao.getMoleculeById(id).molfile();
    }

    public Integer insertMolecule(String molecule) {
        return bingoDao.insertMolecule(indigo.loadMolecule(molecule));
    }

    public void updateMolecule(Integer id, String molecule) {
        bingoDao.deleteMolecule(id);
        bingoDao.insertMolecule(indigo.loadMolecule(molecule), id);
    }

    public void deleteMolecule(Integer id) {
        bingoDao.deleteMolecule(id);
    }

    public String getReaction(Integer id) {
        return bingoDao.getReactionById(id).molfile();
    }

    public Integer insertReaction(String reaction) {
        return bingoDao.insertReaction(indigo.loadReaction(reaction));
    }

    public void updateReaction(Integer id, String reaction) {
        bingoDao.deleteReaction(id);
        bingoDao.insertReaction(indigo.loadReaction(reaction), id);
    }

    public void deleteReaction(Integer id) {
        bingoDao.deleteReaction(id);
    }
}
