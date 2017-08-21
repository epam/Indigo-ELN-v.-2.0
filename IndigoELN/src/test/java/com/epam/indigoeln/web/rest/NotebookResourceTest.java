package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.util.AuthUtil;
import com.epam.indigoeln.web.rest.dto.NotebookDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;

import static com.epam.indigoeln.core.security.CookieConstants.CSRF_TOKEN_HEADER;
import static org.junit.Assert.*;

public class NotebookResourceTest extends RestBase {

    @Autowired
    NotebookRepository notebookRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Test
    public void createNotebook() throws Exception {
        NotebookDTO notebookDTO = new NotebookDTO();
        notebookDTO.setName("Test notebook");
        notebookDTO.setDescription("description");

        RequestEntity<NotebookDTO> requestEntity = RequestEntity.post(new URI("/api/projects/1/notebooks"))
                .header(HttpHeaders.COOKIE, cookie)
                .header(CSRF_TOKEN_HEADER, csrfToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(notebookDTO);

        ResponseEntity<NotebookDTO> responseEntity = restTemplate.exchange(requestEntity, NotebookDTO.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        NotebookDTO result = responseEntity.getBody();

        assertNotNull(result.getId());
        assertEquals(notebookDTO.getDescription(), result.getDescription());
        assertEquals(notebookDTO.getName(), result.getName());
        assertEquals(AuthUtil.login, result.getAuthor().getLogin());
        assertNotNull(result.getCreationDate());
        assertNotNull(result.getLastEditDate());
        assertEquals(AuthUtil.login, result.getLastModifiedBy().getLogin());
        assertEquals(Long.valueOf(0), result.getVersion());

        Notebook savedNotebook = notebookRepository.findOne(result.getFullId());

        assertNotNull(savedNotebook);
        assertEquals(savedNotebook.getDescription(), result.getDescription());
        assertEquals(savedNotebook.getName(), result.getName());
        assertEquals(savedNotebook.getAuthor().getLogin(), result.getAuthor().getLogin());
        assertTrue(savedNotebook.getCreationDate().isEqual(result.getCreationDate()));
        assertTrue(savedNotebook.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(savedNotebook.getLastModifiedBy().getLogin(), result.getLastModifiedBy().getLogin());
        assertEquals(savedNotebook.getVersion(), result.getVersion());

        Project project = projectRepository.findByNotebookId(result.getFullId());
        assertNotNull(project);
    }

    @Test
    public void updateNotebook() throws URISyntaxException {
        Notebook notebook = notebookRepository.findOne("1-1");
        NotebookDTO notebookDTO = new NotebookDTO(notebook);
        notebookDTO.setName("new name");
        notebookDTO.setDescription("new description");

        RequestEntity<NotebookDTO> requestEntity = RequestEntity.put(new URI("/api/projects/1/notebooks"))
                .header(HttpHeaders.COOKIE, cookie)
                .header(CSRF_TOKEN_HEADER, csrfToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(notebookDTO);

        ResponseEntity<NotebookDTO> responseEntity = restTemplate.exchange(requestEntity, NotebookDTO.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        NotebookDTO result = responseEntity.getBody();

        assertEquals(notebookDTO.getFullId(), result.getFullId());
        assertEquals(notebookDTO.getName(), result.getName());
        assertEquals(notebookDTO.getDescription(), result.getDescription());
        assertEquals(notebookDTO.getAuthor().getLogin(), result.getAuthor().getLogin());
        assertTrue(notebookDTO.getCreationDate().isEqual(result.getCreationDate()));
        assertFalse(notebookDTO.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(AuthUtil.login, result.getLastModifiedBy().getLogin());
        assertNotNull(result.getVersion());
        assertEquals(notebookDTO.getVersion() + 1, result.getVersion().longValue());

        Notebook savedNotebook = notebookRepository.findOne(result.getFullId());

        assertEquals(savedNotebook.getName(), result.getName());
        assertEquals(savedNotebook.getDescription(), result.getDescription());
        assertEquals(savedNotebook.getAuthor().getLogin(), result.getAuthor().getLogin());
        assertTrue(savedNotebook.getCreationDate().isEqual(result.getCreationDate()));
        assertTrue(savedNotebook.getLastEditDate().isEqual(result.getLastEditDate()));
        assertEquals(savedNotebook.getLastModifiedBy().getLogin(), result.getLastModifiedBy().getLogin());
        assertEquals(savedNotebook.getVersion(), result.getVersion());
    }

    @Test
    public void getNotebook() throws URISyntaxException {
        RequestEntity<Void> requestEntity = RequestEntity.get(new URI("/api/projects/1/notebooks/1"))
                .header(HttpHeaders.COOKIE, cookie)
                .build();

        ResponseEntity<NotebookDTO> responseEntity = restTemplate.exchange(requestEntity, NotebookDTO.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        NotebookDTO notebookDTO = responseEntity.getBody();

        assertEquals("1-1", notebookDTO.getFullId());
        assertEquals("nb name0", notebookDTO.getName());
        assertEquals("description0", notebookDTO.getDescription());
        assertEquals(AuthUtil.login, notebookDTO.getAuthor().getLogin());
        assertNotNull(notebookDTO.getCreationDate());
        assertNotNull(notebookDTO.getLastEditDate());
        assertEquals(AuthUtil.login, notebookDTO.getLastModifiedBy().getLogin());
        assertNotNull(notebookDTO.getVersion());
    }
}