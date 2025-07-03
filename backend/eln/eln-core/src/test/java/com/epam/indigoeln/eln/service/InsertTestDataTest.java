package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.FeignUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InsertTestDataTest {

    private final Random random = new Random();

    ProjectsClient projectsClient;
    NotebooksClient notebooksClient;
    ExperimentsClient experimentsClient;
    MiscClient miscClient;
    UsersClient usersClient;

    @BeforeEach
    void setup() {
        URI baseURI = URI.create("https://indigo-eln-dev.test.lifescience.opensource.epam.com/");
        AtomicReference<String> testUsername = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>(
                "Bearer eyJraWQiOiJUbTFZSmg5UUJjZkQrVnBKVlc1WEQ3UEV5NEh1d2gxeUlvVlZwcmYxb0UwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJiNGI4MjQwOC1mMDYxLTcwNzQtY2VkMC1kNjFhYWNlOWJjNGIiLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb21cL3VzLWVhc3QtMV82RGlyZ3RRMXAiLCJjbGllbnRfaWQiOiJhNGtraDAwb2IyM2w3dXBtODhoaDNtbWo1Iiwib3JpZ2luX2p0aSI6ImM5N2UzN2Y2LTc0YTItNGE3Yi1hNjQ0LTYwYWExZWFkNmFkZCIsImV2ZW50X2lkIjoiMGM0M2Y3YjgtNjc2MC00OWY3LWI2ZDItMzAyODA2N2ExOGEzIiwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJhd3MuY29nbml0by5zaWduaW4udXNlci5hZG1pbiIsImF1dGhfdGltZSI6MTc1MTIxOTg3MSwiZXhwIjoxNzUxMjk4MDkxLCJpYXQiOjE3NTEyOTQ0OTEsImp0aSI6ImNmYTI4MjEyLWEwOTMtNDllMi1iOTQxLTZlM2JiNGE1NmJiNSIsInVzZXJuYW1lIjoiYWRtaW4ifQ.dFxt0SXaWHx14ogOFLDQhFjM2yVXMysN2I_gCqLuzNAxsmTbYgA5vSPXjfDiRoSB--QPMAYvTdRtL4qOcc8q_ns8dfXeR2nc49t1Ez-4kFPuc-WCps_cYA_jYDWkr5HwEyOMdyH2xvw7RUXBaKiFIPZ7H-1-EzG24_uDtqEIsKIzy7wRzB8qCBEi9TVd-1ApZMIi-nnrhLDKX0CyxtjcApLBMkF5AZoAVlYnTD_zR4Zj18x2Ff6KKQiDVa547DGZBlkRR6LyTsLGVxFlEJ5-XTLKygIlowTJDVybdTeNoMyw8rheix1BfE6aWMHGpxbjbzkBR4jbJU_ryyCOnGGovg"
        );
        projectsClient = FeignUtil.buildFeignClient(baseURI, ProjectsClient.class, testUsername, authorization);
        notebooksClient = FeignUtil.buildFeignClient(baseURI, NotebooksClient.class, testUsername, authorization);
        experimentsClient = FeignUtil.buildFeignClient(baseURI, ExperimentsClient.class, testUsername, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURI, MiscClient.class, testUsername, authorization);
        usersClient = FeignUtil.buildFeignClient(baseURI, UsersClient.class, testUsername, authorization);
    }

//    @Test
    @Order(1)
    void flyway() {
        Object result = miscClient.migrate();
        System.out.println(result);
    }

    @Test
    @Order(2)
    void insertUsers() {
        usersClient.createUser(new UserRequest("alice@eln.com", "Alice Smith", "Alice", "Smith", "password", new ApplicationRole[]{ApplicationRole.CONTENT_EDITOR, ApplicationRole.TEMPLATE_EDITOR}));
        usersClient.createUser(new UserRequest("bob@eln.com", "Bob Johnson", "Bob", "Johnson", "password", new ApplicationRole[]{ApplicationRole.ADMINISTRATOR}));
        usersClient.createUser(new UserRequest("charlie@eln.com", "Charlie Williams", "Charlie", "Williams", "password", new ApplicationRole[]{ApplicationRole.CONTENT_EDITOR}));
    }

//    @Test
    @Order(2)
    void insertTestData(@TempDir Path tempDir) {
        List<DictionaryRef> therapeuticAreas = miscClient.getDictionary(Dictionary.THERAPEUTIC_AREA);
        List<DictionaryRef> projectCodes = miscClient.getDictionary(Dictionary.PROJECT_CODE);
        for (int projectNo = 1; projectNo <= random.nextInt(4, 16); projectNo++) {
            System.out.println("project " + projectNo);
            List<String> keywords = IntStream.range(0, random.nextInt(4)).mapToObj(i -> "keyword" + i).toList();
            ProjectDetailsDTO project = projectsClient.createProject(new ProjectRequest("Test Project " + projectNo, keywords, "literature", "description"));
            for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 2); attachmentNo++) {
                System.out.println("\tattachment " + attachmentNo);
                projectsClient.createProjectAttachment(project.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
            }
            for (int notebookNo = 1; notebookNo <= random.nextInt(1, 4); notebookNo++) {
                System.out.println("\tnotebook " + notebookNo);
                NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest("Test Notebook " + notebookNo, "description"));
                for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                    System.out.println("\tattachment " + attachmentNo);
                    notebooksClient.createNotebookAttachment(notebook.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
                }
                for (int experimentNo = 1; experimentNo <= random.nextInt(1, 12); experimentNo++) {
                    System.out.println("\t\texperiment " + experimentNo);
                    ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("Test Experiment " + experimentNo
                            , "image"
                            , randomOrNone(therapeuticAreas)
                            , randomOrNone(projectCodes)
                    ));
                    for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                        System.out.println("\tattachment " + attachmentNo);
                        experimentsClient.createExperimentAttachment(experiment.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
                    }
                }
            }
        }
    }

    private <T> @Nullable T randomOrNone(List<T> list) {
        int no = random.nextInt(-1, list.size());
        return no == -1 ? null : list.get(no);
    }
}
