package com.epam.indigoeln.core.service.bingodb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import com.epam.indigoeln.core.integration.BingoResult;
import com.epam.indigoeln.core.integration.RestClient;

/**
 * Service for execution BingoDB operations
 */
@Service
public class BingoDbIntegrationService {

    private static final String BINGO_URL_MOLECULE = "%s/molecule";
    private static final String BINGO_URL_GET_OR_UPDATE_MOLECULE = BINGO_URL_MOLECULE + "/%s";

    @Value("${integration.bingodb.url}")
    private String bingoUrl;

    @Value("${integration.bingodb.username}")
    private String bingoUsername;

    @Value("${integration.bingodb.password}")
    private String bingoPassword;

    public String getMolecule(Integer id) {
        String url = String.format(BINGO_URL_GET_OR_UPDATE_MOLECULE, bingoUrl, id);
        BingoResult bingoResult = RestClient.GET(url, RestClient.basicAuthorization(bingoUsername, bingoPassword), BingoResult.class);
        return handleErrorResponse(bingoResult).getStructure();
    }

    public Integer addMolecule(String molfile) {
        String url = String.format(BINGO_URL_MOLECULE, bingoUrl);
        BingoResult bingoResult = RestClient.POST(url, RestClient.basicAuthorization(bingoUsername, bingoPassword), molfile, BingoResult.class);
        return  handleErrorResponse(bingoResult).getId();
    }

    public Integer updateMolecule(Integer id, String molfile) {
        String url = String.format(BINGO_URL_GET_OR_UPDATE_MOLECULE, bingoUrl, id);
        BingoResult bingoResult = RestClient.PUT(url, RestClient.basicAuthorization(bingoUsername, bingoPassword), molfile, BingoResult.class);
        handleErrorResponse(bingoResult);
        return id;
    }

    public void deleteMolecule(Integer id) {
        String url = String.format(BINGO_URL_GET_OR_UPDATE_MOLECULE, bingoUrl, id);
        BingoResult bingoResult = RestClient.DELETE(url, RestClient.basicAuthorization(bingoUsername, bingoPassword), BingoResult.class);
        handleErrorResponse(bingoResult);
    }

    private BingoResult handleErrorResponse(BingoResult bingoResult) {
        if (!bingoResult.getSuccess()) {
            throw new ValidationException("BingoDB request failed with error: " + bingoResult.getErrorMessage());
        }
        return bingoResult;
    }
}
