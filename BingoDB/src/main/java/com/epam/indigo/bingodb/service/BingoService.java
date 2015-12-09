package com.epam.indigo.bingodb.service;

import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.bingodb.repository.BingoRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BingoService {

    @Autowired
    private Indigo indigo;

    @Autowired
    private BingoRepository moleculeRepository;

    @Autowired
    private BingoRepository reactionRepository;

    /* Molecule */

    public String getMolecule(Integer id) {
        return moleculeRepository.getById(id).molfile();
    }

    public Integer insertMolecule(String molecule) {
        return moleculeRepository.insert(indigo.loadMolecule(molecule));
    }

    public void updateMolecule(Integer id, String molecule) {
        moleculeRepository.update(indigo.loadMolecule(molecule), id);
    }

    public void deleteMolecule(Integer id) {
        moleculeRepository.delete(id);
    }

    public List<Integer> searchMoleculeExact(String molecule, String options) {
        return processSearchExact(moleculeRepository, indigo.loadQueryMolecule(molecule), options);
    }

    public List<Integer> searchMoleculeSub(String molecule, String options) {
        return processSearchSub(moleculeRepository, indigo.loadQueryMolecule(molecule), options);
    }

    public List<Integer> searchMoleculeSim(String molecule, Float min, Float max, String metric) {
        return processSearchSim(moleculeRepository, indigo.loadQueryMolecule(molecule), min, max, metric);
    }

    public List<Integer> searchMoleculeMolFormula(String molFormula, String options) {
        return processSearchMolFormula(moleculeRepository, molFormula, options);
    }

    /* Reaction */

    public String getReaction(Integer id) {
        return reactionRepository.getById(id).rxnfile();
    }

    public Integer insertReaction(String reaction) {
        return reactionRepository.insert(indigo.loadReaction(reaction));
    }

    public void updateReaction(Integer id, String reaction) {
        reactionRepository.update(indigo.loadReaction(reaction), id);
    }

    public void deleteReaction(Integer id) {
        reactionRepository.delete(id);
    }

    public List<Integer> searchReactionExact(String reaction, String options) {
        return processSearchExact(reactionRepository, indigo.loadQueryReaction(reaction), options);
    }

    public List<Integer> searchReactionSub(String reaction, String options) {
        return processSearchSub(reactionRepository, indigo.loadQueryReaction(reaction), options);
    }

    public List<Integer> searchReactionSim(String reaction, Float min, Float max, String metric) {
        return processSearchSim(reactionRepository, indigo.loadQueryReaction(reaction), min, max, metric);
    }

    public List<Integer> searchReactionMolFormula(String molFormula, String options) {
        return processSearchMolFormula(reactionRepository, molFormula, options);
    }

    /* Common */

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
        List<Integer> result = new ArrayList<Integer>();

        while (searchResult.next()) {
            result.add(searchResult.getCurrentId());
        }

        return result;
    }
}
