package com.epam.indigoeln.core.service.bingo;

import com.epam.indigo.BingoObject;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.core.repository.bingo.BingoRepository;
import com.epam.indigoeln.core.service.indigo.IndigoProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BingoService {

    @Autowired
    private IndigoProvider indigoProvider;

    @Autowired
    private BingoRepository moleculeRepository;

    @Autowired
    private BingoRepository reactionRepository;

    /* Molecule */

    private static List<Integer> processSearchExact(BingoRepository repository, IndigoObject query, String options) {
        BingoObject searchResult;

        if (StringUtils.isBlank(options)) {
            searchResult = repository.searchExact(query);
        } else {
            searchResult = repository.searchExact(query, options);
        }

        return processSearchResult(searchResult);
    }

    private static List<Integer> processSearchSub(BingoRepository repository, IndigoObject query, String options) {
        BingoObject searchResult;

        if (StringUtils.isBlank(options)) {
            searchResult = repository.searchSub(query);
        } else {
            searchResult = repository.searchSub(query, options);
        }

        return processSearchResult(searchResult);
    }

    private static List<Integer> processSearchSim(BingoRepository repository, IndigoObject query, Float min, Float max, String metric) {
        BingoObject searchResult;

        if (StringUtils.isBlank(metric)) {
            searchResult = repository.searchSim(query, min, max);
        } else {
            searchResult = repository.searchSim(query, min, max, metric);
        }

        return processSearchResult(searchResult);
    }

    private static List<Integer> processSearchMolFormula(BingoRepository repository, String query, String options) {
        BingoObject searchResult;

        if (StringUtils.isBlank(options)) {
            searchResult = repository.searchMolFormula(query);
        } else {
            searchResult = repository.searchMolFormula(query, options);
        }

        return processSearchResult(searchResult);
    }

    private static List<Integer> processSearchResult(BingoObject searchResult) {
        List<Integer> result = new ArrayList<>();

        while (searchResult.next()) {
            result.add(searchResult.getCurrentId());
        }

        return result;
    }

    public Optional<String> getMolecule(Integer id) {
        return moleculeRepository
                .getById(id)
                .map(indigoObject -> Optional.ofNullable(indigoObject.molfile()))
                .orElse(Optional.empty());
    }

    public Optional<Integer> insertMolecule(String molecule) {
        return moleculeRepository.insert(indigoProvider.getIndigo().loadMolecule(molecule));
    }

    public void updateMolecule(Integer id, String molecule) {
        moleculeRepository.update(indigoProvider.getIndigo().loadMolecule(molecule), id);
    }

    public Optional<Boolean> isEmptyMolecule(String reaction) {
        try {
            final IndigoObject moleculeStructure = indigoProvider.getIndigo().loadMolecule(reaction);
            return Optional.of(moleculeStructure.countComponents() == 0);
        } catch (Exception e) {
            return Optional.empty();
        }

    }

    /* Reaction */

    public void deleteMolecule(Integer id) {
        moleculeRepository.delete(id);
    }

    public List<Integer> searchMoleculeExact(String molecule, String options) {
        return processSearchExact(moleculeRepository, indigoProvider.getIndigo().loadMolecule(molecule), options);
    }

    public List<Integer> searchMoleculeSub(String molecule, String options) {
        return processSearchSub(moleculeRepository, indigoProvider.getIndigo().loadQueryMolecule(molecule), options);
    }

    public List<Integer> searchMoleculeSim(String molecule, Float min, Float max, String metric) {
        return processSearchSim(moleculeRepository, indigoProvider.getIndigo().loadMolecule(molecule), min, max, metric);
    }

    public List<Integer> searchMoleculeMolFormula(String formula, String options) {
        return processSearchMolFormula(moleculeRepository, formula, options);
    }

    public Optional<String> getReaction(Integer id) {
        return reactionRepository
                .getById(id)
                .map(indigoObject -> Optional.ofNullable(indigoObject.rxnfile()))
                .orElse(Optional.empty());
    }

    public Optional<Boolean> isEmptyReaction(String reaction) {
        try {
            indigoProvider.getIndigo().loadReaction(reaction);
        } catch (Exception e) {
            try {
                final IndigoObject moleculeStructure = indigoProvider.getIndigo().loadMolecule(reaction);
                if (moleculeStructure.countComponents() == 0) {
                    return Optional.of(true);
                }
            } catch (Exception e1) {
                return Optional.empty();
            }
        }
        return Optional.of(false);
    }

    public Optional<Integer> insertReaction(String reaction) {
        return reactionRepository.insert(indigoProvider.getIndigo().loadReaction(reaction));
    }

    public void updateReaction(Integer id, String reaction) {
        reactionRepository.update(indigoProvider.getIndigo().loadReaction(reaction), id);
    }

    /* Common */

    public void deleteReaction(Integer id) {
        reactionRepository.delete(id);
    }

    public List<Integer> searchReactionExact(String reaction, String options) {
        return processSearchExact(reactionRepository, indigoProvider.getIndigo().loadReaction(reaction), options);
    }

    public List<Integer> searchReactionSub(String reaction, String options) {
        return processSearchSub(reactionRepository, indigoProvider.getIndigo().loadQueryReaction(reaction), options);
    }

    public List<Integer> searchReactionSim(String reaction, Float min, Float max, String metric) {
        return processSearchSim(reactionRepository, indigoProvider.getIndigo().loadReaction(reaction), min, max, metric);
    }

    public List<Integer> searchReactionMolFormula(String formula, String options) {
        return processSearchMolFormula(reactionRepository, formula, options);
    }
}
