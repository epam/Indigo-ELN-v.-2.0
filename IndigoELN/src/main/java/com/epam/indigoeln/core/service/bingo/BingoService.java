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
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BingoService {

    private static final Logger log = LoggerFactory.getLogger(BingoService.class);

    private final ObjectMapper objectMapper;
    private final BingoProperties bingoProperties;

    @Autowired
    public BingoService(ObjectMapper objectMapper, BingoProperties bingoProperties) {
        this.objectMapper = objectMapper;
        this.bingoProperties = bingoProperties;
    }

    /* Common */

    public Optional<String> getById(String id) {
        try {
            BingoResponse response = getResponse("GET", "/structures/" + id, StringUtils.EMPTY);

            if (response.getStructures() != null && !response.getStructures().isEmpty()) {
                return Optional.of(response.getStructures().get(0).getStructure());
            }
        } catch (Exception e) {
            log.warn("Cannot find by id=" + id + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    public Optional<String> insert(String s) {
        try {
            BingoResponse response = getResponse("POST", "/structures", s);

            if (response.getStructures() != null && !response.getStructures().isEmpty()) {
                return Optional.of(response.getStructures().get(0).getId());
            }
        } catch (Exception e) {
            log.warn("Cannot insert: " + e.getMessage());
        }

        return Optional.empty();
    }

    public Optional<String> update(String id, String s) {
        try {
            BingoResponse response = getResponse("PUT", "/structures/" + id, s);

            if (response.getStructures() != null && !response.getStructures().isEmpty()) {
                return Optional.of(response.getStructures().get(0).getId());
            }
        } catch (Exception e) {
            log.warn("Cannot update by id=" + id + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    public void delete(String id) {
        try {
            getResponse("DELETE", "/structures/" + id, StringUtils.EMPTY);
        } catch (Exception e) {
            log.warn("Cannot delete by id=" + id + ": " + e.getMessage());
        }
    }

    /* Molecule */

    public List<String> searchMoleculeExact(String molecule, String options) {
        try {
            if (options == null) {
                options = StringUtils.EMPTY;
            }
            return result(getResponse("POST", "/search/molecule/exact?options=" + options, molecule));
        } catch (Exception e) {
            log.warn("Cannot search exact molecule: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchMoleculeSub(String molecule, String options) {
        try {
            if (options == null) {
                options = StringUtils.EMPTY;
            }
            return result(getResponse("POST", "/search/molecule/substructure?options=" + options, molecule));
        } catch (Exception e) {
            log.warn("Cannot search sub molecule: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchMoleculeSim(String molecule, Float min, Float max, String metric) {
        try {
            if (metric == null) {
                metric = StringUtils.EMPTY;
            }
            return result(getResponse("POST", "/search/molecule/similarity?min=" + min + "&max=" + max + "&metric=" + metric, molecule));
        } catch (Exception e) {
            log.warn("Cannot search sim molecule: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchMoleculeMolFormula(String formula, String options) {
        try {
            if (options == null) {
                options = StringUtils.EMPTY;
            }
            return result(getResponse("POST", "/search/molecule/molformula?options=" + options, formula));
        } catch (Exception e) {
            log.warn("Cannot search molformula molecule: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /* Reaction */

    public List<String> searchReactionExact(String reaction, String options) {
        try {
            if (options == null) {
                options = StringUtils.EMPTY;
            }
            return result(getResponse("POST", "/search/reaction/exact?options=" + options, reaction));
        } catch (Exception e) {
            log.warn("Cannot search exact reaction: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchReactionSub(String reaction, String options) {
        try {
            if (options == null) {
                options = StringUtils.EMPTY;
            }
            return result(getResponse("POST", "/search/reaction/substructure?options=" + options, reaction));
        } catch (Exception e) {
            log.warn("Cannot search sub reaction: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchReactionSim(String reaction, Float min, Float max, String metric) {
        try {
            if (metric == null) {
                metric = StringUtils.EMPTY;
            }
            return result(getResponse("POST", "/search/reaction/similarity?min=" + min + "&max=" + max + "&metric=" + metric, reaction));
        } catch (Exception e) {
            log.warn("Cannot search sim reaction: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<String> searchReactionMolFormula(String formula, String options) {
        try {
            if (options == null) {
                options = StringUtils.EMPTY;
            }
            return result(getResponse("POST", "/search/reaction/molformula?options=" + options, formula));
        } catch (Exception e) {
            log.warn("Cannot search molformula reaction: " + e.getMessage());
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

            connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String(basic.getBytes()));
            connection.setRequestProperty("Content-type", "text/plain");
            connection.setRequestProperty("Accept", "*/*");

            connection.setDoInput(true);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                IOUtils.write(body, os, Charset.forName("UTF-8"));
            }

            if (connection.getResponseCode() == 200) {
                try (InputStream is = connection.getInputStream()) {
                    return objectMapper.readValue(IOUtils.toString(is, Charset.forName("UTF-8")), BingoResponse.class);
                }
            }
        } catch (Exception e) {
            log.warn("Error executing BingoDB request: " + e.getMessage());
        }

        throw new IndigoRuntimeException("Error executing BingoDB request");
    }
}
