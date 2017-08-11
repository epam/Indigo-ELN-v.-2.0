package com.epam.indigoeln.bingodb.web;

import com.epam.indigoeln.bingodb.domain.BingoStructure;
import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class SearchResourceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testSearchMoleculeExact() {
        ResponseDTO molResponse = restTemplate.postForObject("/api/structures", "CC", ResponseDTO.class);
        String id = molResponse.getStructures().get(0).getId();

        ResponseDTO searchResponse = restTemplate.postForObject("/api/search/molecule/exact", "CC", ResponseDTO.class);
        Assert.assertTrue(findById(id, searchResponse.getStructures()).isPresent());
    }

    @Test
    public void testSearchMoleculeSub() {
        ResponseDTO molResponse = restTemplate.postForObject("/api/structures", "CC", ResponseDTO.class);
        String id = molResponse.getStructures().get(0).getId();

        ResponseDTO searchResponse = restTemplate.postForObject("/api/search/molecule/substructure", "CC", ResponseDTO.class);
        Assert.assertTrue(findById(id, searchResponse.getStructures()).isPresent());
    }

    @Test
    public void testSearchMoleculeSimilarity() {
        ResponseDTO molResponse = restTemplate.postForObject("/api/structures", "CC", ResponseDTO.class);
        String id = molResponse.getStructures().get(0).getId();

        ResponseDTO searchResponse = restTemplate.postForObject("/api/search/molecule/similarity?min=0.1&max=1.0", "CC", ResponseDTO.class);
        Assert.assertTrue(findById(id, searchResponse.getStructures()).isPresent());
    }

    @Test
    public void testSearchMoleculeMolFormula() {
        ResponseDTO molResponse = restTemplate.postForObject("/api/structures", "CC", ResponseDTO.class);
        String id = molResponse.getStructures().get(0).getId();

        ResponseDTO searchResponse = restTemplate.postForObject("/api/search/molecule/molformula", "C2H6", ResponseDTO.class);
        Assert.assertTrue(findById(id, searchResponse.getStructures()).isPresent());
    }

    @Test
    public void testSearchReactionExact() {
        ResponseDTO rxnResponse = restTemplate.postForObject("/api/structures", "CC>>CC", ResponseDTO.class);
        String id = rxnResponse.getStructures().get(0).getId();

        ResponseDTO searchResponse = restTemplate.postForObject("/api/search/reaction/exact", "CC>>CC", ResponseDTO.class);
        Assert.assertTrue(findById(id, searchResponse.getStructures()).isPresent());
    }

    @Test
    public void testSearchReactionSub() {
        ResponseDTO rxnResponse = restTemplate.postForObject("/api/structures", "CC>>CC", ResponseDTO.class);
        String id = rxnResponse.getStructures().get(0).getId();

        ResponseDTO searchResponse = restTemplate.postForObject("/api/search/reaction/substructure", "CC>>CC", ResponseDTO.class);
        Assert.assertTrue(findById(id, searchResponse.getStructures()).isPresent());
    }

    @Test
    public void testSearchReactionSimilarity() {
        ResponseDTO rxnResponse = restTemplate.postForObject("/api/structures", "CC>>CC", ResponseDTO.class);
        String id = rxnResponse.getStructures().get(0).getId();

        ResponseDTO searchResponse = restTemplate.postForObject("/api/search/reaction/similarity?min=0.1&max=1.0", "CC>>CC", ResponseDTO.class);
        Assert.assertTrue(findById(id, searchResponse.getStructures()).isPresent());
    }

    private Optional<BingoStructure> findById(String id, List<BingoStructure> structures) {
        return structures.stream().filter(s -> id.equals(s.getId())).findFirst();
    }
}
