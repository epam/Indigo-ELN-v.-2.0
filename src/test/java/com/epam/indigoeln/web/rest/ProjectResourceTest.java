package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import util.AuthUtil;

import java.net.URI;
import java.util.*;

import static com.epam.indigoeln.core.security.CookieConstants.CSRF_TOKEN_HEADER;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProjectResourceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String[] cookie;
    private String csrfToken;

    @Before
    public void getCookies() throws Exception {
        AuthUtil authUtil = new AuthUtil(restTemplate);
        cookie = authUtil.getCookie();
        csrfToken = authUtil.getCsrfToken();
    }

    @Test
    public void createProject() throws Exception {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setDescription("description");
        projectDTO.setKeywords("keyword1 keyword2");
        projectDTO.setReferences("references");
        projectDTO.setTags(Arrays.asList("tag1", "tag2"));
        projectDTO.setName("Test Project");

        RequestEntity<ProjectDTO> requestEntity = RequestEntity.post(new URI("/api/projects"))
                .header(HttpHeaders.COOKIE, cookie)
                .header(CSRF_TOKEN_HEADER, csrfToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(projectDTO);

        ResponseEntity<ProjectDTO> responseEntity = restTemplate.exchange(requestEntity, ProjectDTO.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        ProjectDTO result = responseEntity.getBody();

        assertEquals(projectDTO.getDescription(), result.getDescription());
        assertEquals(projectDTO.getKeywords(), result.getKeywords());
        assertEquals(projectDTO.getReferences(), result.getReferences());
        assertEquals(projectDTO.getTags(), result.getTags());
        assertEquals(projectDTO.getName(), result.getName());
        assertNotNull(result.getId());
    }
}