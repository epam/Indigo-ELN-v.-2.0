package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class DictionaryServiceTest extends ELNBaseTest {

    List<DictionaryItemDTO> items;
    DictionaryItemRef therapeuticArea;
    ExperimentDetailsDTO experiment;

    @BeforeAll
    @SneakyThrows
    void setUpClass() {
        cleanupDatabase();
        withUser(JOHN_USERNAME, () -> {
            therapeuticArea = dictionaryClient.getDictionary(BuiltInDictionary.THERAPEUTIC_AREA).getFirst();
            ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("DictionaryServiceTest"));
            NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
            experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, null, therapeuticArea, null));
        });
    }

    @Test
    @Order(1)
    void testGetDictionaries() {
        List<String> expected = StreamEx.of(BuiltInDictionary.values())
                .map(Enum::name)
                .toList();
        assertThat(dictionaryClient.getDictionaries()).map(DictionaryDTO::getCode).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Order(2)
    void testGetDictionaryFull() {
        List<DictionaryItemDTO> original = dictionaryClient.getDictionaryFull(BuiltInDictionary.THERAPEUTIC_AREA);
        assertThat(original).isNotEmpty();
    }

    @Test
    @Order(3)
    void testAddFirstItem() {
        items = dictionaryClient.addDictionaryItem(BuiltInDictionary.TEST, new DictionaryItemRequest("A", "Adescription"));
        verify(items).containsExactly(
                tuple("A", "Adescription", 1, true)
        );
    }

    @Test
    @Order(4)
    void testAddSecondItem() {
        items = dictionaryClient.addDictionaryItem(BuiltInDictionary.TEST, new DictionaryItemRequest("B", "Bdescription"));
        verify(items).containsExactly(
                tuple("A", "Adescription", 1, true),
                tuple("B", "Bdescription", 2, true)
        );
    }

    @Test
    @Order(5)
    void testUpdateItem() {
        items = dictionaryClient.updateDictionaryItem(BuiltInDictionary.TEST, items.getFirst().getId(), new DictionaryItemEditRequest(
                Optional.of("Anew"),
                Optional.of("AdescriptionNew"),
                null,
                null
        ));
        verify(items).containsExactly(
                tuple("Anew", "AdescriptionNew", 1, true),
                tuple("B", "Bdescription", 2, true)
        );
    }

    @Test
    @Order(6)
    void testDeactivateItem() {
        items = dictionaryClient.updateDictionaryItem(BuiltInDictionary.TEST, items.getFirst().getId(), new DictionaryItemEditRequest(
                null,
                null,
                null,
                Optional.of(false)
        ));
        verify(items).containsExactly(
                tuple("Anew", "AdescriptionNew", 1, false),
                tuple("B", "Bdescription", 2, true)
        );
        assertThat(dictionaryClient.getDictionary(BuiltInDictionary.TEST))
                .extracting(DictionaryItemRef::getName)
                .containsExactly("B");
    }

    @Test
    @Order(7)
    void testAddThirdItem() {
        items = dictionaryClient.addDictionaryItem(BuiltInDictionary.TEST, new DictionaryItemRequest("C", "Cdescription"));
        verify(items).containsExactly(
                tuple("Anew", "AdescriptionNew", 1, false),
                tuple("B", "Bdescription", 2, true),
                tuple("C", "Cdescription", 3, true)
        );
    }

    @Test
    @Order(8)
    void testReorderItems() {
        items = dictionaryClient.updateDictionaryItem(BuiltInDictionary.TEST, items.get(2).getId(), new DictionaryItemEditRequest(
                null,
                null,
                Optional.of(2), // move C to position 2
                null
        ));
        verify(items).containsExactly(
                tuple("Anew", "AdescriptionNew", 1, false),
                tuple("C", "Cdescription", 2, true),
                tuple("B", "Bdescription", 3, true)
        );
    }

    @Test
    @Order(9)
    void testDeleteItem() {
        items = dictionaryClient.removeDictionaryItem(BuiltInDictionary.TEST, items.getFirst().getId());
        verify(items).containsExactly(
                tuple("C", "Cdescription", 1, true),
                tuple("B", "Bdescription", 2, true)
        );
    }

    @Test
    @Order(9)
    void testDeleteItemInUse() {
        assertThatClientCall(() -> {
            dictionaryClient.removeDictionaryItem(BuiltInDictionary.THERAPEUTIC_AREA, therapeuticArea.getId());
        }).isBadRequest("This word is selected in other inputs. Please deactivate the word to remove it from available options of the inputs");
    }

    private AbstractListAssert<?, List<? extends Tuple>, Tuple, ObjectAssert<Tuple>> verify(List<DictionaryItemDTO> items) {
        return assertThat(items).extracting(DictionaryItemDTO::getName, DictionaryItemDTO::getDescription, DictionaryItemDTO::getOrdinal, DictionaryItemDTO::getActive);
    }
}
