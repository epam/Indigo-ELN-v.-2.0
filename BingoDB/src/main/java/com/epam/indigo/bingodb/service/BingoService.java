package com.epam.indigo.bingodb.service;

import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
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
    private IndigoRenderer renderer;

    @Autowired
    private BingoRepository moleculeRepository;

    @Autowired
    private BingoRepository reactionRepository;

    /* Molecule */

    public String getMolecule(Integer id) {
        IndigoObject io = moleculeRepository.getById(id);
        // TODO auto generating coordinates to render structure (bingo nosql does not store coordinates)
        io.layout();
        return io.molfile();
    }

    public byte[] getMoleculePicture(Integer id, Integer width, Integer height) {
        IndigoObject io = moleculeRepository.getById(id);
        if (width != null && height != null)
            indigo.setOption("render-image-size", width, height);
        return renderer.renderToBuffer(io);
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
        return processSearchExact(moleculeRepository, indigo.loadMolecule(molecule), options);
    }

    public List<Integer> searchMoleculeSub(String molecule, String options) {
        return processSearchSub(moleculeRepository, indigo.loadQueryMolecule(molecule), options);
    }

    public List<Integer> searchMoleculeSim(String molecule, Float min, Float max, String metric) {
        return processSearchSim(moleculeRepository, indigo.loadMolecule(molecule), min, max, metric);
    }

    public List<Integer> searchMoleculeMolFormula(String molFormula, String options) {
        return processSearchMolFormula(moleculeRepository, molFormula, options);
    }

    /* Reaction */

    public String getReaction(Integer id) {
        IndigoObject io = reactionRepository.getById(id);
        // TODO auto generating coordinates to render structure (bingo nosql does not store coordinates)
        io.layout();
        return io.rxnfile();
    }

    public byte[] getReactionPicture(Integer id, Integer width, Integer height) {
        IndigoObject io = reactionRepository.getById(id);
        if (width != null && height != null)
            indigo.setOption("render-image-size", width, height);
        return renderer.renderToBuffer(io);
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
        return processSearchExact(reactionRepository, indigo.loadReaction(reaction), options);
    }

    public List<Integer> searchReactionSub(String reaction, String options) {
        return processSearchSub(reactionRepository, indigo.loadQueryReaction(reaction), options);
    }

    public List<Integer> searchReactionSim(String reaction, Float min, Float max, String metric) {
        return processSearchSim(reactionRepository, indigo.loadReaction(reaction), min, max, metric);
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
