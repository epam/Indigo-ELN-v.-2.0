package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.web.rest.dto.ProjectDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import com.epam.indigoeln.util.AuthUtil;
import com.epam.indigoeln.util.DatabaseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.epam.indigoeln.core.security.CookieConstants.CSRF_TOKEN_HEADER;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProjectResourceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DatabaseUtil databaseUtil;

    private String[] cookie;
    private String csrfToken;

    @Before
    public void getCookies() throws Exception {
        AuthUtil authUtil = new AuthUtil(restTemplate);
        cookie = authUtil.getCookie();
        csrfToken = authUtil.getCsrfToken();

        databaseUtil.init();
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
        assertEquals(AuthUtil.login, result.getAuthor().getLogin());
        assertNotNull(result.getCreationDate());
        assertNotNull(result.getLastEditDate());
        assertNotNull(result.getLastModifiedBy());
        assertEquals(Long.valueOf(0), result.getVersion());

        Project projectSaved = projectRepository.findOne(result.getId());

        assertNotNull(projectSaved);
        assertEquals(projectSaved.getDescription(), result.getDescription());
        assertEquals(projectSaved.getKeywords(), result.getKeywords());
        assertEquals(projectSaved.getReferences(), result.getReferences());
        assertEquals(projectSaved.getTags(), result.getTags());
        assertEquals(projectSaved.getName(), result.getName());
        assertEquals(projectSaved.getAuthor().getLogin(), result.getAuthor().getLogin());
        assertTrue(projectSaved.getCreationDate().isEqual(result.getCreationDate()));
        assertTrue(projectSaved.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(projectSaved.getLastModifiedBy().getLogin(), result.getLastModifiedBy().getLogin());
        assertEquals(projectSaved.getVersion(), result.getVersion());

    }

    @Test
    public void updateProject() throws Exception {
        Project project = projectRepository.findOne("1");
        ProjectDTO projectDTO = new ProjectDTO(project);
        projectDTO.setDescription("new description");
        projectDTO.setKeywords("new key");
        projectDTO.setReferences("new references");
        projectDTO.setTags(Arrays.asList("new tag1", "new tag2"));
        projectDTO.setName("new name");

        RequestEntity<ProjectDTO> requestEntity = RequestEntity.put(new URI("/api/projects"))
                .header(HttpHeaders.COOKIE, cookie)
                .header(CSRF_TOKEN_HEADER, csrfToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(projectDTO);

        ResponseEntity<ProjectDTO> responseEntity = restTemplate.exchange(requestEntity, ProjectDTO.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ProjectDTO result = responseEntity.getBody();

        assertEquals(projectDTO.getDescription(), result.getDescription());
        assertEquals(projectDTO.getKeywords(), result.getKeywords());
        assertEquals(projectDTO.getReferences(), result.getReferences());
        assertEquals(projectDTO.getTags(), result.getTags());
        assertEquals(projectDTO.getName(), result.getName());
        assertEquals(projectDTO.getId(), result.getId());
        assertEquals(AuthUtil.login, result.getAuthor().getLogin());
        assertTrue(projectDTO.getCreationDate().isEqual(result.getCreationDate()));
        assertFalse(projectDTO.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(AuthUtil.login, result.getLastModifiedBy().getLogin());
        assertNotNull(result.getVersion());
        assertNotEquals(Long.valueOf(0), result.getVersion());

        Project projectSaved = projectRepository.findOne(result.getId());

        assertNotNull(projectSaved);
        assertEquals(projectSaved.getDescription(), result.getDescription());
        assertEquals(projectSaved.getKeywords(), result.getKeywords());
        assertEquals(projectSaved.getReferences(), result.getReferences());
        assertEquals(projectSaved.getTags(), result.getTags());
        assertEquals(projectSaved.getName(), result.getName());
        assertEquals(projectSaved.getAuthor().getLogin(), result.getAuthor().getLogin());
        assertTrue(projectSaved.getCreationDate().isEqual(result.getCreationDate()));
        assertTrue(projectSaved.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(projectSaved.getLastModifiedBy().getLogin(), result.getLastModifiedBy().getLogin());
        assertEquals(projectSaved.getVersion(), result.getVersion());

    }

    @Test
    public void getProject() throws URISyntaxException {
        RequestEntity<Void> requestEntity = RequestEntity.get(new URI("/api/projects/2"))
                .header(HttpHeaders.COOKIE, cookie)
                .build();

        ResponseEntity<ProjectDTO> responseEntity = restTemplate.exchange(requestEntity, ProjectDTO.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ProjectDTO projectDTO = responseEntity.getBody();

        assertEquals("2", projectDTO.getId());
        assertEquals("Test Project1", projectDTO.getName());
        assertEquals("description1", projectDTO.getDescription());
        assertEquals("keyword1 keyword21", projectDTO.getKeywords());
        assertEquals("references1", projectDTO.getReferences());
        assertEquals(Arrays.asList("tag11", "tag21"), projectDTO.getTags());
        assertEquals(AuthUtil.login, projectDTO.getAuthor().getLogin());

    }
}