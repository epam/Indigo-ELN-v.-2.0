package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class DictionaryServiceTest extends ELNBaseTest {

    static String DICTIONARY_CODE = "TEST";
    
    DictionaryDTO dictionary;
    String dictionaryID;
    boolean dictionaryDeleted;
    List<DictionaryItemDTO> items;
    TherapeuticAreaRef therapeuticArea;
    ExperimentDetailsDTO experiment;

    @BeforeAll
    @SneakyThrows
    void setUpClass() {
        cleanupDatabase();
        withUser(JOHN_USERNAME, () -> {
            therapeuticArea = dictionaryClient.getFirst(BuiltInDictionary.THERAPEUTIC_AREA);
            ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("DictionaryServiceTest"));
            NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
            experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, null, therapeuticArea, null));
        });
    }

    @AfterAll
    void tearDownClass() {
        withUser(JOHN_USERNAME, () -> {
            if (!dictionaryDeleted) {
                dictionaryClient.removeDictionary(DICTIONARY_CODE);
            }
        });
    }

    @Test
    @Order(0)
    void testCreateDictionary() {
        dictionary = dictionaryClient.createDictionary(new DictionaryRequest(DICTIONARY_CODE, "Test", false, "description"));
        assertThat(dictionary.getCode()).isEqualTo(DICTIONARY_CODE);
        assertThat(dictionary.getName()).isEqualTo("Test");
        assertThat(dictionary.getUserEditable()).isFalse();
        assertThat(dictionary.getDescription()).isEqualTo("description");
        dictionaryID = dictionary.getId().toString();
    }

    @Test
    @Order(1)
    void testGetDictionaries() {
        List<String> expected = StreamEx.of(BuiltInDictionary.values())
                .map(Enum::name)
                .toMutableList();
        expected.add(DICTIONARY_CODE);
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
        items = dictionaryClient.addDictionaryItem(dictionaryID, new DictionaryItemRequest("A", "Adescription"));
        verify(items).containsExactly(
                tuple("A", "Adescription", 1, true)
        );
    }

    @Test
    @Order(4)
    void testAddSecondItem() {
        items = dictionaryClient.addDictionaryItem(dictionaryID, new DictionaryItemRequest("B", "Bdescription"));
        verify(items).containsExactly(
                tuple("A", "Adescription", 1, true),
                tuple("B", "Bdescription", 2, true)
        );
    }

    @Test
    @Order(5)
    void testUpdateItem() {
        items = dictionaryClient.updateDictionaryItem(dictionaryID, items.getFirst().getId(), new DictionaryItemEditRequest(
                JsonNullable.of("Anew"),
                JsonNullable.of("AdescriptionNew"),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        ));
        verify(items).containsExactly(
                tuple("Anew", "AdescriptionNew", 1, true),
                tuple("B", "Bdescription", 2, true)
        );
    }

    @Test
    @Order(6)
    void testDeactivateItem() {
        items = dictionaryClient.updateDictionaryItem(dictionaryID, items.getFirst().getId(), new DictionaryItemEditRequest(
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(false)
        ));
        verify(items).containsExactly(
                tuple("Anew", "AdescriptionNew", 1, false),
                tuple("B", "Bdescription", 2, true)
        );
        assertThat(dictionaryClient.getDictionary(dictionaryID))
                .extracting(DictionaryItemRef::getName)
                .containsExactly("B");
    }

    @Test
    @Order(7)
    void testAddThirdItem() {
        items = dictionaryClient.addDictionaryItem(dictionaryID, new DictionaryItemRequest("C", "Cdescription"));
        verify(items).containsExactly(
                tuple("Anew", "AdescriptionNew", 1, false),
                tuple("B", "Bdescription", 2, true),
                tuple("C", "Cdescription", 3, true)
        );
    }

    @Test
    @Order(8)
    void testReorderItems() {
        items = dictionaryClient.updateDictionaryItem(dictionaryID, items.get(2).getId(), new DictionaryItemEditRequest(
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(2), // move C to position 2
                JsonNullable.undefined()
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
        items = dictionaryClient.removeDictionaryItem(dictionaryID, items.getFirst().getId());
        verify(items).containsExactly(
                tuple("C", "Cdescription", 1, true),
                tuple("B", "Bdescription", 2, true)
        );
    }

//    @Test
//    @Order(10)
//    void testDeleteItemInUse() {
//        assertThatClientCall(() -> {
//            dictionaryClient.removeDictionaryItem(BuiltInDictionary.THERAPEUTIC_AREA, therapeuticArea.getId());
//        }).isBadRequest("This word is selected in other inputs. Please deactivate the word to remove it from available options of the inputs");
//    }

    @Test
    @Order(10)
    void testDeleteDictionary() {
        dictionaryClient.removeDictionary(dictionaryID);
        assertThat(dictionaryClient.getDictionaries()).extracting(DictionaryDTO::getCode).doesNotContain(DICTIONARY_CODE);
        dictionaryDeleted = true;
    }

    private AbstractListAssert<?, List<? extends Tuple>, Tuple, ObjectAssert<Tuple>> verify(List<DictionaryItemDTO> items) {
        return assertThat(items).extracting(DictionaryItemDTO::getName, DictionaryItemDTO::getDescription, DictionaryItemDTO::getOrdinal, DictionaryItemDTO::getActive);
    }
}
