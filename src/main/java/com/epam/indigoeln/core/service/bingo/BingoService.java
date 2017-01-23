package com.epam.indigoeln.core.service.bingo;

import com.epam.indigoeln.config.bingo.IndigoConfig;
import com.epam.indigoeln.core.repository.bingo.BingoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BingoService {

    @Autowired
    private IndigoConfig indigoConfig;

    @Autowired
    private BingoRepository moleculeRepository;

    @Autowired
    private BingoRepository reactionRepository;

    /* Molecule */

    public Optional<Boolean> isEmptyMolecule(String molecule) {
        try {
            return Optional.of(indigoConfig.indigo().loadMolecule(molecule).countComponents() == 0);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> getMolecule(Integer id) {
        return Optional.ofNullable(moleculeRepository.getById(id));
    }

    public Optional<Integer> insertMolecule(String molecule) {
        return Optional.ofNullable(moleculeRepository.insert(molecule));
    }

    public void updateMolecule(Integer id, String molecule) {
        moleculeRepository.update(molecule, id);
    }

    public void deleteMolecule(Integer id) {
        moleculeRepository.delete(id);
    }

    public List<Integer> searchMoleculeExact(String molecule, String options) {
        return moleculeRepository.searchExact(molecule, options);
    }

    public List<Integer> searchMoleculeSub(String molecule, String options) {
        return moleculeRepository.searchSub(molecule, options);
    }

    public List<Integer> searchMoleculeSim(String molecule, Float min, Float max, String metric) {
        return moleculeRepository.searchSim(molecule, min, max, metric);
    }

    public List<Integer> searchMoleculeMolFormula(String formula, String options) {
        return moleculeRepository.searchMolFormula(formula, options);
    }

    /* Reaction */

    public Optional<Boolean> isEmptyReaction(String reaction) {
        try {
            indigoConfig.indigo().loadReaction(reaction);
        } catch (Exception e) {
            try {
                if (indigoConfig.indigo().loadMolecule(reaction).countComponents() == 0) {
                    return Optional.of(true);
                }
            } catch (Exception e1) {
                return Optional.empty();
            }
        }
        return Optional.of(false);
    }

    public Optional<String> getReaction(Integer id) {
        return Optional.ofNullable(reactionRepository.getById(id));
    }

    public Optional<Integer> insertReaction(String reaction) {
        return Optional.ofNullable(reactionRepository.insert(reaction));
    }

    public void updateReaction(Integer id, String reaction) {
        reactionRepository.update(reaction, id);
    }

    public void deleteReaction(Integer id) {
        reactionRepository.delete(id);
    }

    public List<Integer> searchReactionExact(String reaction, String options) {
        return reactionRepository.searchExact(reaction, options);
    }

    public List<Integer> searchReactionSub(String reaction, String options) {
        return reactionRepository.searchSub(reaction, options);
    }

    public List<Integer> searchReactionSim(String reaction, Float min, Float max, String metric) {
        return reactionRepository.searchSim(reaction, min, max, metric);
    }

    public List<Integer> searchReactionMolFormula(String formula, String options) {
        return reactionRepository.searchMolFormula(formula, options);
    }
}
