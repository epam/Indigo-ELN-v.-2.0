package com.epam.indigoeln.bingoservice.service;

import com.epam.indigo.Indigo;
import com.epam.indigoeln.bingoservice.repository.BingoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BingoService {

    @Autowired
    private Indigo indigo;

    @Autowired
    private BingoRepository moleculeRepository;

    @Autowired
    private BingoRepository reactionRepository;

    public String getMolecule(Integer id) {
        return moleculeRepository.getById(id).molfile();
    }

    public Integer insertMolecule(String molecule) {
        return moleculeRepository.insert(indigo.loadMolecule(molecule));
    }

    public void updateMolecule(Integer id, String molecule) {
        moleculeRepository.delete(id);
        moleculeRepository.insert(indigo.loadMolecule(molecule), id);
    }

    public void deleteMolecule(Integer id) {
        moleculeRepository.delete(id);
    }

    public String getReaction(Integer id) {
        return reactionRepository.getById(id).rxnfile();
    }

    public Integer insertReaction(String reaction) {
        return reactionRepository.insert(indigo.loadReaction(reaction));
    }

    public void updateReaction(Integer id, String reaction) {
        reactionRepository.delete(id);
        reactionRepository.insert(indigo.loadReaction(reaction), id);
    }

    public void deleteReaction(Integer id) {
        reactionRepository.delete(id);
    }
}
