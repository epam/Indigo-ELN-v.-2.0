package com.epam.indigoeln.bingodb.web;

import com.epam.indigoeln.bingodb.config.IndigoProvider;
import com.epam.indigoeln.bingodb.web.rest.dto.ResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class StructureResourceTest {

    @Autowired
    private IndigoProvider indigoProvider;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testInsert() {
        ResponseDTO molResponse = restTemplate.postForObject("/api/structures", "CC", ResponseDTO.class);

        Assert.assertEquals(1, molResponse.getStructures().size());
        Assert.assertNotNull(molResponse.getStructures().get(0).getId());
        Assert.assertTrue(molResponse.getStructures().get(0).getId().startsWith("MOL"));

        ResponseDTO rxnResponse = restTemplate.postForObject("/api/structures", "CC>>CC", ResponseDTO.class);

        Assert.assertEquals(1, rxnResponse.getStructures().size());
        Assert.assertNotNull(rxnResponse.getStructures().get(0).getId());
        Assert.assertTrue(rxnResponse.getStructures().get(0).getId().startsWith("RXN"));

        ResponseDTO badResponse = restTemplate.postForObject("/api/structures", "o_O", ResponseDTO.class);

        Assert.assertEquals("Error processing request: Not a molecule or reaction", badResponse.getMessage());
        Assert.assertNull(badResponse.getStructures());
    }

    @Test
    public void testUpdate() {
        ResponseDTO molResponse = restTemplate.postForObject("/api/structures", "CC", ResponseDTO.class);

        String id = molResponse.getStructures().get(0).getId();

        ResponseEntity<ResponseDTO> badResponse = restTemplate.exchange("/api/structures/" + id, HttpMethod.PUT, new HttpEntity<>("o_O"), ResponseDTO.class);

        Assert.assertEquals("Error processing request: Not a molecule or reaction", badResponse.getBody().getMessage());
        Assert.assertNull(badResponse.getBody().getStructures());

        ResponseEntity<ResponseDTO> updResponse = restTemplate.exchange("/api/structures/" + id, HttpMethod.PUT, new HttpEntity<>("CC>>CC"), ResponseDTO.class);

        Assert.assertEquals(1, updResponse.getBody().getStructures().size());
        Assert.assertNotNull(updResponse.getBody().getStructures().get(0).getId());
        Assert.assertTrue(updResponse.getBody().getStructures().get(0).getId().startsWith("RXN"));
    }

    @Test
    public void testDelete() {
        ResponseDTO molResponse = restTemplate.postForObject("/api/structures", "CC", ResponseDTO.class);

        String id = molResponse.getStructures().get(0).getId();

        ResponseDTO getResponse = restTemplate.getForObject("/api/structures/" + id, ResponseDTO.class);
        Assert.assertEquals(id, getResponse.getStructures().get(0).getId());

        ResponseEntity<ResponseDTO> delResponse = restTemplate.exchange("/api/structures/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, ResponseDTO.class);

        Assert.assertEquals(HttpStatus.OK, delResponse.getStatusCode());
        Assert.assertNull(delResponse.getBody());

        delResponse = restTemplate.exchange("/api/structures/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, ResponseDTO.class);
        Assert.assertTrue(StringUtils.contains(delResponse.getBody().getMessage(), "Error processing request"));
        Assert.assertEquals("Error processing request: There is no object with this id", delResponse.getBody().getMessage());

        getResponse = restTemplate.getForObject("/api/structures/" + id, ResponseDTO.class);
        Assert.assertTrue(StringUtils.contains(getResponse.getMessage(), "Error processing request"));
    }

    @Test
    public void testGetById() {
        ResponseDTO molResponse = restTemplate.postForObject("/api/structures", "CC", ResponseDTO.class);

        String id = molResponse.getStructures().get(0).getId();

        ResponseDTO getResponse = restTemplate.getForObject("/api/structures/" + id, ResponseDTO.class);

        Assert.assertEquals(id, getResponse.getStructures().get(0).getId());
        Assert.assertEquals(indigoProvider.indigo().loadMolecule("CC").molfile(), getResponse.getStructures().get(0).getStructure());

        ResponseDTO rxnResponse = restTemplate.postForObject("/api/structures", "CC>>CC", ResponseDTO.class);

        id = rxnResponse.getStructures().get(0).getId();

        getResponse = restTemplate.getForObject("/api/structures/" + id, ResponseDTO.class);

        Assert.assertEquals(id, getResponse.getStructures().get(0).getId());
        Assert.assertEquals(indigoProvider.indigo().loadReaction("CC>>CC").rxnfile(), getResponse.getStructures().get(0).getStructure());
    }
}
