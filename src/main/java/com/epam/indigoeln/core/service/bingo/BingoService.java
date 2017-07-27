package com.epam.indigoeln.core.service.bingo;

import com.epam.indigoeln.IndigoRuntimeException;
import com.epam.indigoeln.config.bingo.BingoProperties;
import com.epam.indigoeln.core.service.bingo.dto.BingoResponse;
import com.epam.indigoeln.core.service.bingo.dto.BingoStructure;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BingoService {

    private static final Logger log = LoggerFactory.getLogger(BingoService.class);

    @Autowired
    private BingoProperties bingoProperties;

    /* Common */

    public Optional<String> getById(String id) {
        try {
            BingoResponse response = getResponse(HttpMethod.GET, "/structures/" + id, StringUtils.EMPTY);

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
            BingoResponse response = getResponse(HttpMethod.POST, "/structures", s);

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
            BingoResponse response = getResponse(HttpMethod.PUT, "/structures/" + id, s);

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
            getResponse(HttpMethod.DELETE, "/structures/" + id, StringUtils.EMPTY);
        } catch (Exception e) {
            log.warn("Cannot delete by id=" + id + ": " + e.getMessage());
        }
    }

    private List<String> result(BingoResponse response) {
        List<String> result = new ArrayList<>();

        if (response.getStructures() != null) {
            for (BingoStructure structure : response.getStructures()) {
                result.add(structure.getId());
            }
        }

        return result;
    }

    private BingoResponse getResponse(HttpMethod method, String endpoint, String body) {
        String basic = bingoProperties.getUsername() + ":" + bingoProperties.getPassword();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.encodeBase64String(basic.getBytes()));
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<BingoResponse> resp = new RestTemplate().exchange(
                bingoProperties.getApiUrl() + endpoint,
                method,
                requestEntity,
                BingoResponse.class);

        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }

        throw new IndigoRuntimeException("Error executing BingoDB request");
    }

    /* Molecule */

    public List<String> searchMoleculeExact(String molecule, String options) {
        try {
            if (options == null) {
                options = StringUtils.EMPTY;
            }
            return result(getResponse(HttpMethod.POST, "/search/molecule/exact?options=" + options, molecule));
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
            return result(getResponse(HttpMethod.POST, "/search/molecule/substructure?options=" + options, molecule));
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
            return result(getResponse(HttpMethod.POST, "/search/molecule/similarity?min=" + min + "&max=" + max + "&metric=" + metric, molecule));
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
            return result(getResponse(HttpMethod.POST, "/search/molecule/molformula?options=" + options, formula));
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
            return result(getResponse(HttpMethod.POST, "/search/reaction/exact?options=" + options, reaction));
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
            return result(getResponse(HttpMethod.POST, "/search/reaction/substructure?options=" + options, reaction));
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
            return result(getResponse(HttpMethod.POST, "/search/reaction/similarity?min=" + min + "&max=" + max + "&metric=" + metric, reaction));
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
            return result(getResponse(HttpMethod.POST, "/search/reaction/molformula?options=" + options, formula));
        } catch (Exception e) {
            log.warn("Cannot search molformula reaction: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
