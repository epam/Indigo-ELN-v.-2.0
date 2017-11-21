package com.epam.indigoeln.core.service.bingo;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.config.bingo.BingoProperties;
import com.epam.indigoeln.core.service.bingo.dto.BingoResponse;
import com.epam.indigoeln.core.service.bingo.dto.BingoStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The BingoService provides a number of methods for
 * molecule's data manipulation
 *
 * @author Vladislav Alekseev
 */
@Service
public class BingoService {

    /**
     * Logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BingoService.class);
    public static final String STRUCTURES = "/structures/";

    /**
     * ObjectMapper instance for reading JSON
     */
    @Autowired
    private ObjectMapper objectMapper;


    /**
     * BingoProperties instance for getting BingoDB properties
     */
    @Autowired
    private BingoProperties bingoProperties;

    /* Common */

    /**
     * Returns chemical structure of molecule by id from BingoDB
     *
     * @param id The chemical structure's id
     * @return a non-empty string representation of chemical structure if It's exist
     */
    public Optional<String> getById(String id) {
        try {
            BingoResponse response = getResponse("GET", STRUCTURES + id, StringUtils.EMPTY);

            if (response.getStructures() != null && !response.getStructures().isEmpty()) {
                return Optional.of(response.getStructures().get(0).getStructure());
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot find by id=" + id + ": " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Inserts chemical structure of molecule to BingoDB
     *
     * @param s String representation of chemical structure
     * @return a non-empty string representation of chemical structure
     * if insert was successful, otherwise it returns empty string
     */
    public Optional<String> insert(String s) {
        try {
            BingoResponse response = getResponse("POST", "/structures", s);

            if (response.getStructures() != null && !response.getStructures().isEmpty()) {
                return Optional.of(response.getStructures().get(0).getId());
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot insert: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Updates chemical structure of molecule in BingoDB
     *
     * @param id The chemical structure's id
     * @param s  New string representation of chemical structure
     * @return updated string representation of chemical structure
     * if update was successful, otherwise it returns empty string
     */
    public Optional<String> update(String id, String s) {
        try {
            BingoResponse response = getResponse("PUT", STRUCTURES + id, s);

            if (response.getStructures() != null && !response.getStructures().isEmpty()) {
                return Optional.of(response.getStructures().get(0).getId());
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot update by id=" + id + ": " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Deletes chemical structure of molecule by id
     *
     * @param id The chemical structure's id
     */
    public void delete(String id) {
        try {
            getResponse("DELETE", STRUCTURES + id, StringUtils.EMPTY);
        } catch (Exception e) {
            LOGGER.warn("Cannot delete by id=" + id + ": " + e.getMessage(), e);
        }
    }

    /* Molecule */

    /**
     * Searches for exact molecule
     *
     * @param molecule Molecule structure to search for
     * @param options  Search options
     * @return The list of string molecule's representations
     * if search was successful, otherwise it returns empty list
     */
    public List<String> searchMoleculeExact(String molecule, String options) {
        try {
            String opts = options == null ? StringUtils.EMPTY : options;
            return result(getResponse("POST", "/search/molecule/exact?options=" + opts, molecule));
        } catch (Exception e) {
            LOGGER.warn("Cannot search exact molecule: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Searches for molecule by substructure
     *
     * @param molecule Molecule substructure to search for
     * @param options  Search options
     * @return The list of string molecule's representations
     * if search was successful, otherwise it returns empty list
     */
    public List<String> searchMoleculeSub(String molecule, String options) {
        try {
            String opts = options == null ? StringUtils.EMPTY : options;
            return result(getResponse("POST", "/search/molecule/substructure?options=" + opts, molecule));
        } catch (Exception e) {
            LOGGER.warn("Cannot search sub molecule: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Searches for molecule by similarity
     *
     * @param molecule Molecule substructure to search for
     * @param min      Min similarity
     * @param max      Max similarity
     * @param metric   Search options
     * @return The list of string molecule's representations
     * if search was successful, otherwise it returns empty list
     */
    public List<String> searchMoleculeSim(String molecule, Float min, Float max, String metric) {
        try {
            String mtrc = metric == null ? StringUtils.EMPTY : metric;
            return result(getResponse("POST", "/search/molecule/similarity?min=" + min + "&max=" + max + "&metric=" + mtrc, molecule));
        } catch (Exception e) {
            LOGGER.warn("Cannot search sim molecule: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Searches for molecule by formula
     *
     * @param formula Molecule formula to search for
     * @param options Search options
     * @return The list of string molecule's representations
     * if search was successful, otherwise it returns empty list
     */
    public List<String> searchMoleculeMolFormula(String formula, String options) {
        try {
            String opts = options == null ? StringUtils.EMPTY : options;
            return result(getResponse("POST", "/search/molecule/molformula?options=" + opts, formula));
        } catch (Exception e) {
            LOGGER.warn("Cannot search molformula molecule: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /* Reaction */

    /**
     * Searches for exact reaction
     *
     * @param reaction Reaction structure to search for
     * @param options  Search options
     * @return The list of string reaction's representations
     * if search was successful, otherwise it returns empty list
     */
    public List<String> searchReactionExact(String reaction, String options) {
        try {
            String opts = options == null ? StringUtils.EMPTY : options;
            return result(getResponse("POST", "/search/reaction/exact?options=" + opts, reaction));
        } catch (Exception e) {
            LOGGER.warn("Cannot search exact reaction: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Searches for reaction by substructure
     *
     * @param reaction Reaction substructure to search for
     * @param options  Search options
     * @return The list of string reaction's representations
     * if search was successful, otherwise it returns empty list
     */
    public List<String> searchReactionSub(String reaction, String options) {
        try {
            String opts = options == null ? StringUtils.EMPTY : options;
            return result(getResponse("POST", "/search/reaction/substructure?options=" + opts, reaction));
        } catch (Exception e) {
            LOGGER.warn("Cannot search sub reaction: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Searches for reaction by similarity
     *
     * @param reaction Reaction substructure to search for
     * @param min      Min similarity
     * @param max      Max similarity
     * @param metric   Search options
     * @return The list of string reaction's representations
     * if search was successful, otherwise it returns empty list
     */
    public List<String> searchReactionSim(String reaction, Float min, Float max, String metric) {
        try {
            String mtrc = metric == null ? StringUtils.EMPTY : metric;
            return result(getResponse("POST", "/search/reaction/similarity?min=" + min + "&max=" + max + "&metric=" + mtrc, reaction));
        } catch (Exception e) {
            LOGGER.warn("Cannot search sim reaction: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Searches for reaction by formula
     *
     * @param formula Reaction formula to search for
     * @param options Search options
     * @return The list of string reaction's representations
     * if search was successful, otherwise it returns empty list
     */
    public List<String> searchReactionMolFormula(String formula, String options) {
        try {
            String opts = options == null ? StringUtils.EMPTY : options;
            return result(getResponse("POST", "/search/reaction/molformula?options=" + opts, formula));
        } catch (Exception e) {
            LOGGER.warn("Cannot search molformula reaction: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /* Private Common */

    private List<String> result(BingoResponse response) {
        List<String> result = new ArrayList<>();

        if (response.getStructures() != null) {
            for (BingoStructure structure : response.getStructures()) {
                result.add(structure.getId());
            }
        }

        return result;
    }

    private BingoResponse getResponse(String method, String endpoint, String body) {
        String basic = bingoProperties.getUsername() + ":" + bingoProperties.getPassword();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(bingoProperties.getApiUrl() + endpoint).openConnection();

            connection.setConnectTimeout(60 * 1000);
            connection.setReadTimeout(60 * 1000);

            connection.setRequestMethod(method);

            connection.setRequestProperty(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String(basic.getBytes()));
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "text/plain");
            connection.setRequestProperty(HttpHeaders.ACCEPT, "*/*");

            connection.setDoInput(true);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                IOUtils.write(StringUtils.isBlank(body) ? StringUtils.EMPTY : body, os, Charset.forName("UTF-8"));
            }

            if (connection.getResponseCode() == 200) {
                try (InputStream is = connection.getInputStream()) {
                    return objectMapper.readValue(IOUtils.toString(is, Charset.forName("UTF-8")), BingoResponse.class);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error executing BingoDB request: " + e.getMessage(), e);
        }

        throw new IndigoRuntimeException("Error executing BingoDB request");
    }
}
