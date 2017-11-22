package com.epam.indigoeln.bingodb.service;

import com.epam.indigo.Bingo;
import com.epam.indigo.BingoObject;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.bingodb.config.IndigoProvider;
import com.epam.indigoeln.bingodb.domain.BingoStructure;
import com.epam.indigoeln.bingodb.exception.BingoDbException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for work with structure databases.
 */
@Service
public class BingoService {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BingoService.class);

    /**
     * Prefix for molecule IDs.
     */
    private static final String PREFIX_MOL = "MOL";

    /**
     * Prefix for reaction IDs.
     */
    private static final String PREFIX_RXN = "RXN";

    /**
     * Error text for case if structure is not a molecule or reaction.
     */
    private static final String ERROR_NOT_A_MOLECULE_OR_REACTION = "Not a molecule or reaction";

    /**
     * Error text for case if structure is not a molecule.
     */
    private static final String ERROR_NOT_A_MOLECULE = "Not a molecule";

    /**
     * Error text for case if structure is not a reaction.
     */
    private static final String ERROR_NOT_A_REACTION = "Not a reaction";

    /**
     * IndigoProvider instance which provides Indigo instances with common properties.
     */
    private final IndigoProvider indigoProvider;

    /**
     * Indigo instance for molecule database Bingo instance.
     */
    private final Indigo moleculeIndigo;

    /**
     * Indigo instance for reaction database Bingo instance.
     */
    private final Indigo reactionIndigo;

    /**
     * Bingo instance for molecule database.
     */
    private final Bingo moleculeBingo;

    /**
     * Bingo instance for reaction database.
     */
    private final Bingo reactionBingo;

    /**
     * Mutex for locking Bingo databases simultaneous usage.
     */
    private final Object lock = new Object();

    /**
     * Create a new BingoService instance.
     *
     * @param indigoProvider IndigoProvider instance which provides Indigo instances with common properties
     * @param moleculeIndigo Indigo instance for molecule database Bingo instance
     * @param reactionIndigo Indigo instance for reaction database Bingo instance
     * @param moleculeBingo  Bingo instance for molecule database
     * @param reactionBingo  Bingo instance for reaction database
     */
    @Autowired
    public BingoService(IndigoProvider indigoProvider,
                        Indigo moleculeIndigo,
                        Indigo reactionIndigo,
                        Bingo moleculeBingo,
                        Bingo reactionBingo) {
        this.indigoProvider = indigoProvider;
        this.moleculeIndigo = moleculeIndigo;
        this.reactionIndigo = reactionIndigo;
        this.moleculeBingo = moleculeBingo;
        this.reactionBingo = reactionBingo;
    }

    /**
     * Retrieve structure with given ID.
     *
     * @param id structure ID
     * @return found structure with its ID
     */
    public BingoStructure getById(String id) {
        synchronized (lock) {
            if (isMoleculeId(id)) {
                return new BingoStructure(id, moleculeBingo.getRecordById(getIntId(id)).molfile());
            }
            if (isReactionId(id)) {
                return new BingoStructure(id, reactionBingo.getRecordById(getIntId(id)).rxnfile());
            }
            return null;
        }
    }

    /**
     * Insert a new structure.
     *
     * @param s molecule or reaction in Molfile/Rxnfile/Smiles format
     * @return inserted structure with its ID
     */
    public BingoStructure insert(String s) {
        synchronized (lock) {
            if (isMolecule(s)) {
                return getById(createId(moleculeBingo.insert(moleculeIndigo.loadMolecule(s)), s));
            }
            if (isReaction(s)) {
                return getById(createId(reactionBingo.insert(reactionIndigo.loadReaction(s)), s));
            }
            throw new BingoDbException(ERROR_NOT_A_MOLECULE_OR_REACTION);
        }
    }

    /**
     * Update structure with given ID.
     *
     * @param id structure ID
     * @param s  molecule or reaction in Molfile/Rxnfile/Smiles format
     * @return updated structure with its ID
     */
    public BingoStructure update(String id, String s) {
        synchronized (lock) {
            if (!isMolecule(s) && !isReaction(s)) {
                throw new BingoDbException(ERROR_NOT_A_MOLECULE_OR_REACTION);
            }
            delete(id);
            return insert(s);
        }
    }

    /**
     * Delete structure with given ID.
     *
     * @param id structure ID
     */
    public void delete(String id) {
        synchronized (lock) {
            if (isMoleculeId(id)) {
                moleculeBingo.delete(getIntId(id));
            }
            if (isReactionId(id)) {
                reactionBingo.delete(getIntId(id));
            }
        }
    }

    /**
     * Search molecules with exact match.
     *
     * @param s       molecule in Molfile/Smiles format
     * @param options search options
     * @return found molecules with their IDs
     */
    public List<BingoStructure> searchMoleculeExact(String s, String options) {
        synchronized (lock) {
            if (isMolecule(s)) {
                return result(moleculeBingo.searchExact(moleculeIndigo.loadMolecule(s), options), PREFIX_MOL);
            }
            throw new BingoDbException(ERROR_NOT_A_MOLECULE);
        }
    }

    /**
     * Search molecules with substructure match.
     *
     * @param s       molecule in Molfile/Smiles format
     * @param options search options
     * @return found molecules with their IDs
     */
    public List<BingoStructure> searchMoleculeSub(String s, String options) {
        synchronized (lock) {
            if (isMolecule(s)) {
                return result(moleculeBingo.searchSub(moleculeIndigo.loadQueryMolecule(s), options), PREFIX_MOL);
            }
            throw new BingoDbException(ERROR_NOT_A_MOLECULE);
        }
    }

    /**
     * Search molecules with similarity match.
     *
     * @param s      molecule in Molfile/Smiles format
     * @param min    similarity min value
     * @param max    similarity max value
     * @param metric similarity metric (default is 'tanimoto')
     * @return found molecules with their IDs
     */
    public List<BingoStructure> searchMoleculeSim(String s, Float min, Float max, String metric) {
        synchronized (lock) {
            if (isMolecule(s)) {
                return result(moleculeBingo.searchSim(moleculeIndigo.loadMolecule(s), min, max, metric), PREFIX_MOL);
            }
            throw new BingoDbException(ERROR_NOT_A_MOLECULE);
        }
    }

    /**
     * Search molecules by molecular formula.
     *
     * @param molFormula molecular formula
     * @param options    search options
     * @return found molecules with their IDs
     */
    public List<BingoStructure> searchMoleculeMolFormula(String molFormula, String options) {
        synchronized (lock) {
            return result(moleculeBingo.searchMolFormula(molFormula, options), PREFIX_MOL);
        }
    }

    /**
     * Search reactions with exact match.
     *
     * @param s       molecule or reaction in Molfile/Rxnfile/Smiles format
     * @param options search options
     * @return found reactions with their IDs
     */
    public List<BingoStructure> searchReactionExact(String s, String options) {
        synchronized (lock) {
            if (isReaction(s)) {
                return result(reactionBingo.searchExact(reactionIndigo.loadReaction(s), options), PREFIX_RXN);
            }
            if (isMolecule(s)) {
                List<BingoStructure> result = new ArrayList<>();

                IndigoObject rxn1 = reactionIndigo.createReaction();
                rxn1.addReactant(reactionIndigo.loadMolecule(s));
                result.addAll(result(reactionBingo.searchExact(rxn1, options), PREFIX_RXN));

                IndigoObject rxn2 = reactionIndigo.createReaction();
                rxn2.addProduct(reactionIndigo.loadMolecule(s));
                result.addAll(result(reactionBingo.searchExact(rxn2, options), PREFIX_RXN));

                return result;
            }
            throw new BingoDbException(ERROR_NOT_A_REACTION);
        }
    }

    /**
     * Search reactions with substructure match.
     *
     * @param s       molecule or reaction in Molfile/Rxnfile/Smiles format
     * @param options search options
     * @return found reactions with their IDs
     */
    public List<BingoStructure> searchReactionSub(String s, String options) {
        synchronized (lock) {
            if (isReaction(s)) {
                return result(reactionBingo.searchSub(reactionIndigo.loadQueryReaction(s), options), PREFIX_RXN);
            }
            if (isMolecule(s)) {
                List<BingoStructure> result = new ArrayList<>();

                IndigoObject rxn1 = reactionIndigo.createQueryReaction();
                rxn1.addReactant(reactionIndigo.loadMolecule(s));
                result.addAll(result(reactionBingo.searchSub(rxn1, options), PREFIX_RXN));

                IndigoObject rxn2 = reactionIndigo.createQueryReaction();
                rxn2.addProduct(reactionIndigo.loadMolecule(s));
                result.addAll(result(reactionBingo.searchSub(rxn2, options), PREFIX_RXN));

                return result;
            }
            throw new BingoDbException(ERROR_NOT_A_REACTION);
        }
    }

    /**
     * Search reactions with similarity match.
     *
     * @param s      molecule or reaction in Molfile/Rxnfile/Smiles format
     * @param min    similarity min
     * @param max    similarity max
     * @param metric similarity metric (default is 'tanimoto')
     * @return found reactions with their IDs
     */
    public List<BingoStructure> searchReactionSim(String s, Float min, Float max, String metric) {
        synchronized (lock) {
            if (isReaction(s)) {
                return result(reactionBingo.searchSim(reactionIndigo.loadReaction(s), min, max, metric), PREFIX_RXN);
            }
            if (isMolecule(s)) {
                List<BingoStructure> result = new ArrayList<>();

                IndigoObject rxn1 = reactionIndigo.createReaction();
                rxn1.addReactant(reactionIndigo.loadMolecule(s));
                result.addAll(result(reactionBingo.searchSim(rxn1, min, max, metric), PREFIX_RXN));

                IndigoObject rxn2 = reactionIndigo.createReaction();
                rxn2.addProduct(reactionIndigo.loadMolecule(s));
                result.addAll(result(reactionBingo.searchSim(rxn2, min, max, metric), PREFIX_RXN));

                return result;
            }
            throw new BingoDbException(ERROR_NOT_A_REACTION);
        }
    }

    /**
     * Create search results mapping Bingo search result to structures and create IDs.
     *
     * @param o      Bingo search result
     * @param prefix prefix to create ID
     * @return found structures with their IDs
     */
    private List<BingoStructure> result(BingoObject o, String prefix) {
        List<BingoStructure> result = new ArrayList<>();

        while (o.next()) {
            result.add(getById(prefix + o.getCurrentId()));
        }

        return result;
    }

    /**
     * Get structure Bingo ID from string ID.
     *
     * @param id structure string ID
     * @return structure Bingo ID
     */
    private Integer getIntId(String id) {
        if (isMoleculeId(id)) {
            return Integer.valueOf(id.substring(id.indexOf(PREFIX_MOL) + PREFIX_MOL.length()));
        }
        if (isReactionId(id)) {
            return Integer.valueOf(id.substring(id.indexOf(PREFIX_RXN) + PREFIX_RXN.length()));
        }
        return Integer.valueOf(id);
    }

    /**
     * Create structure string ID from Bingo ID with prefix.
     *
     * @param id structure Bingo ID
     * @param s  string prefix (for molecule or reaction)
     * @return structure string ID with prefix
     */
    private String createId(Integer id, String s) {
        if (isMolecule(s)) {
            return PREFIX_MOL + id;
        }
        if (isReaction(s)) {
            return PREFIX_RXN + id;
        }
        return Integer.toString(id);
    }

    /**
     * Check if structure string ID is a molecule ID.
     *
     * @param id structure string ID
     * @return true if structure string ID is a molecule ID and false otherwise
     */
    private boolean isMoleculeId(String id) {
        return StringUtils.startsWith(id, PREFIX_MOL);
    }

    /**
     * Check if structure string ID is a reaction ID.
     *
     * @param id structure string ID
     * @return true if structure string ID is a reaction ID and false otherwise
     */
    private boolean isReactionId(String id) {
        return StringUtils.startsWith(id, PREFIX_RXN);
    }

    /**
     * Check if structure is a molecule.
     *
     * @param s structure to check
     * @return true if structure is a molecule and false otherwise
     */
    private boolean isMolecule(String s) {
        try {
            indigoProvider.indigo().loadMolecule(s);
            return true;
        } catch (Exception e) {
            LOGGER.trace("isMolecule", e);
        }
        return false;
    }

    /**
     * Check if structure is a reaction.
     *
     * @param s structure to check
     * @return true if structure is a reaction and false otherwise
     */
    private boolean isReaction(String s) {
        try {
            indigoProvider.indigo().loadReaction(s);
            return true;
        } catch (Exception e) {
            LOGGER.trace("isReaction", e);
        }
        return false;
    }
}
