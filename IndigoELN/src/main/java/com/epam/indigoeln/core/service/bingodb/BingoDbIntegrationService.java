package com.epam.indigoeln.core.service.bingodb;

import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.epam.indigoeln.core.integration.BingoResult;

/**
 * Service for execution BingoDB operations
 */
@Service
public class BingoDbIntegrationService {

    private static final String BINGO_URL_MOLECULE = "%s/molecule";
    private static final String BINGO_URL_REACTION = "%s/reaction";
    private static final String BINGO_URL_GET_OR_UPDATE_MOLECULE = BINGO_URL_MOLECULE + "/%s";
    private static final String BINGO_URL_GET_OR_UPDATE_REACTION = BINGO_URL_REACTION + "/%s";


    @Value("${integration.bingodb.url}")
    private String bingoUrl;

    @Value("${integration.bingodb.username}")
    private String bingoUsername;

    @Value("${integration.bingodb.password}")
    private String bingoPassword;

    /**
     * Get molecule by Bingo DB Id
     * @param id bingo DB id
     * @return structure of molecule (molfile)
     */
    public Optional<String> getMolecule(Integer id) {
        return Optional.ofNullable(
                execute(String.format(BINGO_URL_GET_OR_UPDATE_MOLECULE, bingoUrl, id), HttpMethod.GET, null).getStructure());
    }

    /**
     * Add molecule to BingoDB and return Bingo DB id
     * @param molfile structure of molecule
     * @return result of operation
     */
    public BingoResult addMolecule(String molfile) {
        return execute(String.format(BINGO_URL_MOLECULE, bingoUrl), HttpMethod.POST, molfile);
    }

    /**
     * Update existing molecule by id
     * @param id id of existing molecule
     * @param molfile structure of molecule (molfile)
     * @return result of operation
     */
    public BingoResult updateMolecule(Integer id, String molfile) {
        return execute(String.format(BINGO_URL_GET_OR_UPDATE_MOLECULE, bingoUrl, id), HttpMethod.PUT, molfile);
    }

    /**
     * Delete molecule from Bingo DB by id
     * @param id id of deleted molecule
     */
    public void deleteMolecule(Integer id) {
        execute(String.format(BINGO_URL_GET_OR_UPDATE_MOLECULE, bingoUrl, id), HttpMethod.DELETE, null);
    }

    /**
     * Get reaction by Bingo DB Id
     * @param id bingo DB id
     * @return structure of reaction (molfile)
     */
    public Optional<String> getReaction(Integer id) {
        return Optional.ofNullable(
                execute(String.format(BINGO_URL_GET_OR_UPDATE_REACTION, bingoUrl, id), HttpMethod.GET, null).getStructure());
    }

    /**
     * Add reaction to BingoDB and return Bingo DB id
     * @param molfile structure of reaction
     * @return result of operation
     */
    public BingoResult addReaction(String molfile) {
        return execute(String.format(BINGO_URL_REACTION, bingoUrl), HttpMethod.POST, molfile);
    }

    /**
     * Update existing reaction by id
     * @param id id of existing reaction
     * @param molfile structure of reaction (molfile)
     * @return result of operation
     */
    public BingoResult updateReaction(Integer id, String molfile) {
        return execute(String.format(BINGO_URL_GET_OR_UPDATE_REACTION, bingoUrl, id), HttpMethod.PUT, molfile);
    }

    /**
     * Delete reaction from Bingo DB by id
     * @param id id of deleted reaction
     */
    public void deleteReaction(Integer id) {
        execute(String.format(BINGO_URL_GET_OR_UPDATE_REACTION, bingoUrl, id), HttpMethod.DELETE, null);
    }

    /**
     * Execute REST request
     * @param url request url
     * @param method request method
     * @param content request content
     * @return result of request
     */
    private BingoResult execute(String url, HttpMethod method, String content) {
        RestTemplate template = new RestTemplate();
        ResponseEntity<BingoResult> response = template.exchange(url, method, basicAuthorization(content), BingoResult.class);
        //handle invalid http status code
        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new HttpClientErrorException(response.getStatusCode());
        }
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    private HttpEntity basicAuthorization(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        String base64Credentials = new String(
                Base64.getEncoder().encode(String.format("%s:%s", bingoUsername, bingoPassword).getBytes()));
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64Credentials);
        return new HttpEntity(requestBody, headers);
    }
}
